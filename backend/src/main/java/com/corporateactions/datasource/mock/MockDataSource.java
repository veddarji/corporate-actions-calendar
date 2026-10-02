package com.corporateactions.datasource.mock;

import com.corporateactions.corporateaction.entity.EventType;
import com.corporateactions.datasource.CorporateActionDataSource;
import com.corporateactions.datasource.CorporateActionRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock data source - DISABLED by default.
 * Set datasource.mock.enabled=true only if specifically requested for unit testing.
 * Strictly forbidden in production.
 */
@Component
@ConditionalOnProperty(name = "datasource.mock.enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class MockDataSource implements CorporateActionDataSource {

    @Override
    public String getSourceName() {
        return "MOCK";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public int getPriority() {
        return 999; // Lowest priority - only used when real sources fail
    }

    @Override
    public List<CorporateActionRecord> fetchCorporateActions(String companySymbol, LocalDate from, LocalDate to) {
        log.debug("MockDataSource generating data for {}", companySymbol);
        return generateMockData(companySymbol, from, to);
    }

    @Override
    public List<CorporateActionRecord> fetchAllRecentCorporateActions(LocalDate from, LocalDate to) {
        List<CorporateActionRecord> all = new ArrayList<>();
        String[] symbols = {"RELIANCE", "TCS", "HDFCBANK", "INFY", "HINDUNILVR",
                "ICICIBANK", "KOTAKBANK", "BAJFINANCE", "WIPRO", "AXISBANK"};
        for (String symbol : symbols) {
            all.addAll(generateMockData(symbol, from, to));
        }
        return all;
    }

    private List<CorporateActionRecord> generateMockData(String symbol, LocalDate from, LocalDate to) {
        List<CorporateActionRecord> records = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // Generate a dividend event
        LocalDate divExDate = today.plusDays(7);
        records.add(CorporateActionRecord.builder()
                .sourceRecordId(symbol + "_DIV_" + divExDate)
                .source("MOCK")
                .companySymbol(symbol)
                .exchange("NSE")
                .eventType(EventType.DIVIDEND)
                .announcementDate(today.minusDays(15))
                .exDate(divExDate)
                .recordDate(divExDate.plusDays(1))
                .paymentDate(divExDate.plusDays(30))
                .amount(new BigDecimal("5.00"))
                .currency("INR")
                .dividendType("Interim Dividend")
                .build());

        // Generate an earnings event
        LocalDate earningsDate = today.plusDays(14);
        records.add(CorporateActionRecord.builder()
                .sourceRecordId(symbol + "_EARN_Q2FY2027")
                .source("MOCK")
                .companySymbol(symbol)
                .exchange("NSE")
                .eventType(EventType.EARNINGS)
                .announcementDate(earningsDate.minusDays(7))
                .effectiveDate(earningsDate)
                .quarter("Q2")
                .financialYear("FY2027")
                .description("Quarterly Results - Q2 FY2027")
                .build());

        // Generate a bonus issue for some symbols
        if (symbol.equals("TCS") || symbol.equals("RELIANCE") || symbol.equals("INFY")) {
            LocalDate bonusDate = today.plusDays(30);
            records.add(CorporateActionRecord.builder()
                    .sourceRecordId(symbol + "_BONUS_" + bonusDate)
                    .source("MOCK")
                    .companySymbol(symbol)
                    .exchange("NSE")
                    .eventType(EventType.BONUS_ISSUE)
                    .announcementDate(bonusDate.minusDays(21))
                    .exDate(bonusDate)
                    .recordDate(bonusDate.plusDays(1))
                    .ratioFrom(new BigDecimal("1"))
                    .ratioTo(new BigDecimal("1"))
                    .description("Bonus Issue 1:1")
                    .build());
        }

        // Generate AGM for some
        if (symbol.equals("HDFCBANK") || symbol.equals("ICICIBANK")) {
            LocalDate agmDate = today.plusDays(45);
            records.add(CorporateActionRecord.builder()
                    .sourceRecordId(symbol + "_AGM_2027")
                    .source("MOCK")
                    .companySymbol(symbol)
                    .exchange("NSE")
                    .eventType(EventType.AGM)
                    .announcementDate(agmDate.minusDays(30))
                    .effectiveDate(agmDate)
                    .meetingType("AGM")
                    .description("Annual General Meeting 2027")
                    .build());
        }

        return records;
    }
}
