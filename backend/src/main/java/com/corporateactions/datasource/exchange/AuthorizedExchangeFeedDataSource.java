package com.corporateactions.datasource.exchange;

import com.corporateactions.corporateaction.entity.EventType;
import com.corporateactions.datasource.CorporateActionDataSource;
import com.corporateactions.datasource.CorporateActionRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for Authorized / Licensed Exchange Data Feeds (e.g., NSE Data & Analytics Ltd / DotEx EOD feed).
 *
 * This adapter enables licensed enterprise feeds (via SFTP drop folder or authorized API key)
 * to be plugged in cleanly without changing the calendar, database, authentication, or frontend.
 */
@Component
@ConditionalOnProperty(name = "datasource.authorized-feed.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
@Slf4j
public class AuthorizedExchangeFeedDataSource implements CorporateActionDataSource {

    @Value("${datasource.authorized-feed.directory:/data/exchange-feeds}")
    private String feedDirectoryPath;

    @Value("${datasource.authorized-feed.api-url:}")
    private String feedApiUrl;

    @Value("${datasource.authorized-feed.api-key:}")
    private String feedApiKey;

    @Override
    public String getSourceName() {
        return "NSE_AUTHORIZED_FEED";
    }

    @Override
    public boolean isAvailable() {
        File dir = new File(feedDirectoryPath);
        return (dir.exists() && dir.isDirectory()) || (feedApiUrl != null && !feedApiUrl.isBlank());
    }

    @Override
    public int getPriority() {
        return 1; // Highest priority - official licensed feed
    }

    @Override
    public List<CorporateActionRecord> fetchCorporateActions(String companySymbol, LocalDate from, LocalDate to) {
        List<CorporateActionRecord> records = new ArrayList<>();
        File dir = new File(feedDirectoryPath);

        if (!dir.exists() || !dir.isDirectory()) {
            return records;
        }

        try {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".csv") || name.endsWith(".json"));
            if (files == null) return records;

            for (File file : files) {
                List<String> lines = Files.readAllLines(file.toPath());
                for (String line : lines) {
                    if (line.startsWith("SYMBOL") || line.isBlank()) continue;
                    String[] parts = line.split(",");
                    if (parts.length >= 6 && parts[0].trim().equalsIgnoreCase(companySymbol)) {
                        records.add(CorporateActionRecord.builder()
                                .source("NSE_AUTHORIZED_FEED")
                                .sourceRecordId("AUTH_NSE_" + parts[0].trim() + "_" + parts[1].trim())
                                .companySymbol(parts[0].trim())
                                .exchange("NSE")
                                .eventType(parseEventType(parts[2].trim()))
                                .exDate(LocalDate.parse(parts[3].trim()))
                                .recordDate(LocalDate.parse(parts[4].trim()))
                                .description(parts[5].trim())
                                .rawPayload(line)
                                .build());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error reading authorized exchange feed for {}: {}", companySymbol, e.getMessage());
        }

        return records;
    }

    private EventType parseEventType(String val) {
        try {
            return EventType.valueOf(val.toUpperCase());
        } catch (Exception e) {
            return EventType.OTHER;
        }
    }
}
