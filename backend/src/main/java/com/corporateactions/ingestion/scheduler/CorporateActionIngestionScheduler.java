package com.corporateactions.ingestion.scheduler;

import com.corporateactions.datasource.CorporateActionDataSource;
import com.corporateactions.ingestion.service.CorporateActionIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Background scheduler that continuously maintains the corporate actions database.
 *
 * This is the core component that makes the platform "proactive":
 * - Users NEVER trigger data fetching
 * - This scheduler runs continuously in the background
 * - Users read pre-computed, already-fetched data from PostgreSQL/Redis
 *
 * Schedules are configurable via application.yml (data.ingestion.* properties)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CorporateActionIngestionScheduler {

    private final CorporateActionIngestionService ingestionService;
    private final List<CorporateActionDataSource> dataSources;

    private final AtomicBoolean ingestionRunning = new AtomicBoolean(false);

    /**
     * Primary ingestion job - runs every 15 minutes.
     * Fetches the latest corporate actions from all available sources.
     * If a previous run is still in progress, this invocation is skipped.
     */
    @Scheduled(fixedDelayString = "${data.ingestion.corporate-actions.interval:900000}")
    @Async("ingestionTaskExecutor")
    public void runCorporateActionIngestion() {
        if (!ingestionRunning.compareAndSet(false, true)) {
            log.info("Ingestion already running, skipping this cycle");
            return;
        }
        try {
            log.info("Starting scheduled corporate actions ingestion");
            List<CorporateActionDataSource> available = dataSources.stream()
                    .filter(CorporateActionDataSource::isAvailable)
                    .sorted(java.util.Comparator.comparingInt(CorporateActionDataSource::getPriority))
                    .toList();

            if (available.isEmpty()) {
                log.warn("No data sources available, skipping ingestion cycle");
                return;
            }

            ingestionService.runIngestion(available, "SCHEDULED_INGESTION");
            log.info("Scheduled ingestion completed");
        } catch (Exception e) {
            log.error("Scheduled ingestion failed: {}", e.getMessage(), e);
        } finally {
            ingestionRunning.set(false);
        }
    }

    /**
     * Nightly reconciliation job - runs at 2 AM every day.
     * Does a more thorough cross-source validation pass.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Async("ingestionTaskExecutor")
    public void runNightlyReconciliation() {
        log.info("Starting nightly reconciliation job");
        try {
            List<CorporateActionDataSource> all = dataSources.stream()
                    .sorted(java.util.Comparator.comparingInt(CorporateActionDataSource::getPriority))
                    .toList();
            ingestionService.runIngestion(all, "NIGHTLY_RECONCILIATION");
            log.info("Nightly reconciliation completed");
        } catch (Exception e) {
            log.error("Nightly reconciliation failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Allow manual trigger from admin panel.
     */
    public void triggerManualIngestion() {
        log.info("Manual ingestion triggered by admin");
        runCorporateActionIngestion();
    }
}
