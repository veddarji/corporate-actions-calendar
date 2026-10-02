package com.corporateactions.ingestion.service;

import com.corporateactions.company.entity.Company;
import com.corporateactions.company.repository.CompanyRepository;
import com.corporateactions.corporateaction.entity.*;
import com.corporateactions.corporateaction.repository.CorporateActionRepository;
import com.corporateactions.datasource.CorporateActionDataSource;
import com.corporateactions.datasource.CorporateActionRecord;
import com.corporateactions.ingestion.entity.IngestionJobLog;
import com.corporateactions.ingestion.entity.IngestionJobLog.JobStatus;
import com.corporateactions.ingestion.entity.RawMarketData;
import com.corporateactions.ingestion.repository.IngestionJobLogRepository;
import com.corporateactions.ingestion.repository.RawMarketDataRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Orchestrates the full data ingestion pipeline:
 * Fetch → Store Raw → Normalize → Validate → Deduplicate → Upsert → Cache Invalidate
 *
 * This service runs ONLY in background scheduler threads.
 * User requests NEVER wait for this service to complete.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CorporateActionIngestionService {

    private final CorporateActionRepository corporateActionRepository;
    private final CompanyRepository companyRepository;
    private final RawMarketDataRepository rawMarketDataRepository;
    private final IngestionJobLogRepository jobLogRepository;
    private final ObjectMapper objectMapper;
    private final CacheManager cacheManager;

    /**
     * Run the full ingestion pipeline for all active companies.
     */
    public IngestionJobLog runIngestion(List<CorporateActionDataSource> sources, String jobName) {
        IngestionJobLog jobLog = IngestionJobLog.builder()
                .jobName(jobName)
                .source(sources.stream().map(CorporateActionDataSource::getSourceName)
                        .reduce("", (a, b) -> a.isEmpty() ? b : a + "," + b))
                .status(JobStatus.RUNNING)
                .startedAt(OffsetDateTime.now())
                .recordsFetched(0)
                .recordsProcessed(0)
                .recordsInserted(0)
                .recordsUpdated(0)
                .recordsSkipped(0)
                .build();
        jobLog = jobLogRepository.save(jobLog);

        int fetched = 0, inserted = 0, updated = 0, skipped = 0;

        try {
            List<Company> activeCompanies = companyRepository.findAllActive();
            LocalDate from = LocalDate.now().minusYears(2);
            LocalDate to = LocalDate.now().plusMonths(6);

            for (Company company : activeCompanies) {
                for (CorporateActionDataSource source : sources) {
                    if (!source.isAvailable()) {
                        log.warn("Source {} not available, skipping", source.getSourceName());
                        continue;
                    }
                    try {
                        List<CorporateActionRecord> records = source.fetchCorporateActions(
                                company.getSymbol(), from, to);
                        fetched += records.size();

                        for (CorporateActionRecord record : records) {
                            try {
                                ProcessResult result = processRecord(record, company);
                                switch (result) {
                                    case INSERTED -> inserted++;
                                    case UPDATED -> updated++;
                                    case SKIPPED -> skipped++;
                                }
                            } catch (Exception e) {
                                log.error("Failed to process record {} from {}: {}",
                                        record.getSourceRecordId(), source.getSourceName(), e.getMessage());
                                skipped++;
                            }
                        }
                    } catch (Exception e) {
                        log.error("Failed to fetch from {} for {}: {}",
                                source.getSourceName(), company.getSymbol(), e.getMessage());
                    }
                }
            }

            // Invalidate all caches after ingestion
            invalidateCaches();

            jobLog.setStatus(JobStatus.COMPLETED);
            log.info("Ingestion completed: fetched={}, inserted={}, updated={}, skipped={}",
                    fetched, inserted, updated, skipped);

        } catch (Exception e) {
            log.error("Ingestion job failed: {}", e.getMessage(), e);
            jobLog.setStatus(JobStatus.FAILED);
            jobLog.setErrorMessage(e.getMessage());
        }

        jobLog.setCompletedAt(OffsetDateTime.now());
        jobLog.setRecordsFetched(fetched);
        jobLog.setRecordsProcessed(fetched);
        jobLog.setRecordsInserted(inserted);
        jobLog.setRecordsUpdated(updated);
        jobLog.setRecordsSkipped(skipped);
        return jobLogRepository.save(jobLog);
    }

    @Transactional
    public ProcessResult processRecord(CorporateActionRecord record, Company company) {
        // 1. Compute checksum of raw payload
        String checksum = computeChecksum(record.getRawPayload() != null ?
                record.getRawPayload() : objectMapper.valueToTree(record).toString());

        // 2. Check if raw data already stored (idempotent)
        if (rawMarketDataRepository.existsBySourceAndChecksum(record.getSource(), checksum)) {
            return ProcessResult.SKIPPED;
        }

        // 3. Store raw data
        RawMarketData rawData = RawMarketData.builder()
                .source(record.getSource())
                .sourceRecordId(record.getSourceRecordId())
                .companySymbol(record.getCompanySymbol())
                .payloadJson(record.getRawPayload() != null ? record.getRawPayload() : "{}")
                .checksum(checksum)
                .build();
        rawMarketDataRepository.save(rawData);

        // 4. Validate
        if (!isValid(record)) {
            rawData.setProcessingError("Validation failed");
            rawMarketDataRepository.save(rawData);
            return ProcessResult.SKIPPED;
        }

        // 5. Deduplicate - check if we already have this event
        Optional<CorporateAction> existing = (record.getSourceRecordId() != null)
                ? corporateActionRepository.findBySourceAndSourceRecordId(
                        record.getSource(), record.getSourceRecordId())
                : Optional.empty();

        ProcessResult result;
        if (existing.isPresent()) {
            // 6a. Update if changed and refresh last verified timestamp
            CorporateAction ca = existing.get();
            updateIfChanged(ca, record);
            ca.setLastVerifiedAt(OffsetDateTime.now());
            corporateActionRepository.save(ca);
            result = ProcessResult.UPDATED;
        } else {
            // 6b. Insert new
            CorporateAction ca = mapToEntity(record, company);
            corporateActionRepository.save(ca);
            result = ProcessResult.INSERTED;
        }

        rawData.setProcessed(true);
        rawData.setProcessedAt(OffsetDateTime.now());
        rawMarketDataRepository.save(rawData);

        return result;
    }

    private CorporateAction mapToEntity(CorporateActionRecord r, Company company) {
        return CorporateAction.builder()
                .company(company)
                .eventType(r.getEventType())
                .announcementDate(r.getAnnouncementDate())
                .exDate(r.getExDate())
                .recordDate(r.getRecordDate())
                .paymentDate(r.getPaymentDate())
                .effectiveDate(r.getEffectiveDate())
                .amount(r.getAmount())
                .currency(r.getCurrency() != null ? r.getCurrency() : "INR")
                .ratioFrom(r.getRatioFrom())
                .ratioTo(r.getRatioTo())
                .issuePrice(r.getIssuePrice())
                .subscriptionStartDate(r.getSubscriptionStartDate())
                .subscriptionEndDate(r.getSubscriptionEndDate())
                .quarter(r.getQuarter())
                .financialYear(r.getFinancialYear())
                .meetingType(r.getMeetingType())
                .buybackPrice(r.getBuybackPrice())
                .buybackQuantity(r.getBuybackQuantity())
                .periodStartDate(r.getPeriodStartDate())
                .periodEndDate(r.getPeriodEndDate())
                .dividendType(r.getDividendType())
                .description(r.getDescription())
                .source(r.getSource())
                .sourceRecordId(r.getSourceRecordId())
                .sourceReference(r.getSource() + ":" + r.getSourceRecordId())
                .lastVerifiedAt(OffsetDateTime.now())
                .dataQuality(DataQualityStatus.VERIFIED)
                .status(CorporateActionStatus.ANNOUNCED)
                .build();
    }

    private void updateIfChanged(CorporateAction ca, CorporateActionRecord r) {
        boolean changed = false;
        if (!Objects.equals(ca.getExDate(), r.getExDate())) { ca.setExDate(r.getExDate()); changed = true; }
        if (!Objects.equals(ca.getRecordDate(), r.getRecordDate())) { ca.setRecordDate(r.getRecordDate()); changed = true; }
        if (!Objects.equals(ca.getPaymentDate(), r.getPaymentDate())) { ca.setPaymentDate(r.getPaymentDate()); changed = true; }
        if (!Objects.equals(ca.getAmount(), r.getAmount())) { ca.setAmount(r.getAmount()); changed = true; }
        if (changed) {
            ca.setDataQuality(DataQualityStatus.PENDING_REVIEW);
            ca.setSourceUpdatedAt(OffsetDateTime.now());
        }
    }

    private boolean isValid(CorporateActionRecord r) {
        if (r.getEventType() == null) return false;
        if (r.getCompanySymbol() == null || r.getCompanySymbol().isBlank()) return false;
        // Record date must be >= ex-date
        if (r.getExDate() != null && r.getRecordDate() != null && r.getRecordDate().isBefore(r.getExDate())) {
            log.warn("Invalid dates: recordDate {} before exDate {} for {}", r.getRecordDate(), r.getExDate(), r.getCompanySymbol());
            return false;
        }
        // Amount must be positive if present
        if (r.getAmount() != null && r.getAmount().signum() < 0) return false;
        return true;
    }

    private void invalidateCaches() {
        try {
            cacheManager.getCacheNames().forEach(name -> {
                var cache = cacheManager.getCache(name);
                if (cache != null) cache.clear();
            });
            log.debug("All caches invalidated after ingestion");
        } catch (Exception e) {
            log.warn("Cache invalidation failed: {}", e.getMessage());
        }
    }

    private String computeChecksum(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return String.valueOf(content.hashCode());
        }
    }

    public enum ProcessResult { INSERTED, UPDATED, SKIPPED }
}
