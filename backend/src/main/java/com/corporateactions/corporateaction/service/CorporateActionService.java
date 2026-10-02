package com.corporateactions.corporateaction.service;

import com.corporateactions.corporateaction.dto.CorporateActionDto;
import com.corporateactions.corporateaction.entity.CorporateAction;
import com.corporateactions.corporateaction.entity.EventType;
import com.corporateactions.corporateaction.repository.CorporateActionRepository;
import com.corporateactions.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CorporateActionService {

    private final CorporateActionRepository repository;

    @Cacheable(value = "company-events", key = "#companyId + '-' + #page")
    public Page<CorporateActionDto> getByCompany(Long companyId, int page, int size) {
        return repository.findByCompanyId(companyId, PageRequest.of(page, size))
                .map(this::toDto);
    }

    public CorporateActionDto getById(Long id) {
        return repository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Corporate action not found: " + id));
    }

    /**
     * CRITICAL: This method queries the DATABASE/CACHE only.
     * It NEVER calls external APIs. Data is maintained by background ingestion jobs.
     */
    @Transactional(readOnly = true)
    public List<CorporateActionDto> getCalendarEvents(List<Long> companyIds,
                                                       LocalDate from,
                                                       LocalDate to) {
        if (companyIds == null || companyIds.isEmpty()) {
            return List.of();
        }
        return repository.findByCompanyIdsAndDateRange(companyIds, from, to)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CorporateActionDto> getUpcoming(List<Long> companyIds) {
        if (companyIds == null || companyIds.isEmpty()) {
            return List.of();
        }
        return repository.findUpcoming(companyIds, LocalDate.now())
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public CorporateActionDto toDto(CorporateAction ca) {
        return CorporateActionDto.builder()
                .id(ca.getId())
                .companyId(ca.getCompany().getId())
                .companyName(ca.getCompany().getName())
                .companySymbol(ca.getCompany().getSymbol())
                .companyExchange(ca.getCompany().getExchange())
                .eventType(ca.getEventType())
                .eventTypeLabel(formatEventType(ca.getEventType()))
                .announcementDate(ca.getAnnouncementDate())
                .exDate(ca.getExDate())
                .recordDate(ca.getRecordDate())
                .paymentDate(ca.getPaymentDate())
                .effectiveDate(ca.getEffectiveDate())
                .amount(ca.getAmount())
                .currency(ca.getCurrency())
                .ratioFrom(ca.getRatioFrom())
                .ratioTo(ca.getRatioTo())
                .issuePrice(ca.getIssuePrice())
                .subscriptionStartDate(ca.getSubscriptionStartDate())
                .subscriptionEndDate(ca.getSubscriptionEndDate())
                .quarter(ca.getQuarter())
                .financialYear(ca.getFinancialYear())
                .meetingType(ca.getMeetingType())
                .buybackPrice(ca.getBuybackPrice())
                .buybackQuantity(ca.getBuybackQuantity())
                .periodStartDate(ca.getPeriodStartDate())
                .periodEndDate(ca.getPeriodEndDate())
                .dividendType(ca.getDividendType())
                .description(ca.getDescription())
                .status(ca.getStatus())
                .dataQuality(ca.getDataQuality())
                .source(ca.getSource())
                .sourceReference(ca.getSourceReference())
                .sourceUpdatedAt(ca.getSourceUpdatedAt())
                .lastVerifiedAt(ca.getLastVerifiedAt())
                .createdAt(ca.getCreatedAt())
                .updatedAt(ca.getUpdatedAt())
                .build();
    }

    private String formatEventType(EventType type) {
        return switch (type) {
            case DIVIDEND -> "Dividend";
            case STOCK_SPLIT -> "Stock Split";
            case BONUS_ISSUE -> "Bonus Issue";
            case RIGHTS_ISSUE -> "Rights Issue";
            case EARNINGS -> "Earnings";
            case AGM -> "AGM";
            case EGM -> "EGM";
            case BUYBACK -> "Buyback";
            case OTHER -> "Other";
        };
    }
}
