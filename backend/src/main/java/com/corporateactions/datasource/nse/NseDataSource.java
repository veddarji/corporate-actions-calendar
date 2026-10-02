package com.corporateactions.datasource.nse;

import com.corporateactions.corporateaction.entity.EventType;
import com.corporateactions.datasource.CorporateActionDataSource;
import com.corporateactions.datasource.CorporateActionRecord;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * NSE (National Stock Exchange) data source adapter.
 * Fetches corporate action data from NSE's public API.
 *
 * NOTE: This class is ONLY called from background scheduler threads.
 * It is NEVER called from user request threads.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NseDataSource implements CorporateActionDataSource {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${nse.base-url:https://www.nseindia.com}")
    private String nseBaseUrl;

    @Value("${nse.corporate-actions-url:https://www.nseindia.com/api/corporates-corporateActions}")
    private String corporateActionsUrl;

    @Value("${nse.user-agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36}")
    private String userAgent;

    private static final DateTimeFormatter NSE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private static final DateTimeFormatter ALT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public String getSourceName() {
        return "NSE";
    }

    @Override
    public boolean isAvailable() {
        try {
            HttpHeaders headers = buildHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    nseBaseUrl, HttpMethod.GET, entity, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("NSE availability check failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public int getPriority() {
        return 1; // Highest priority
    }

    @Override
    public List<CorporateActionRecord> fetchCorporateActions(String companySymbol, LocalDate from, LocalDate to) {
        List<CorporateActionRecord> records = new ArrayList<>();
        try {
            // Fetch dividends
            records.addAll(fetchDividends(companySymbol, from, to));
            // Fetch bonus/split/rights
            records.addAll(fetchCorporateActionsFromNse(companySymbol, from, to));
        } catch (Exception e) {
            log.error("NSE fetch failed for {}: {}", companySymbol, e.getMessage());
        }
        return records;
    }

    @Override
    public List<CorporateActionRecord> fetchAllRecentCorporateActions(LocalDate from, LocalDate to) {
        List<CorporateActionRecord> records = new ArrayList<>();
        try {
            String url = corporateActionsUrl + "?index=equities";
            String responseBody = makeRequest(url);
            if (responseBody != null) {
                records.addAll(parseNseCorporateActions(responseBody, null));
            }
        } catch (Exception e) {
            log.error("NSE bulk fetch failed: {}", e.getMessage());
        }
        return records;
    }

    private List<CorporateActionRecord> fetchDividends(String symbol, LocalDate from, LocalDate to) {
        List<CorporateActionRecord> records = new ArrayList<>();
        try {
            String url = corporateActionsUrl + "?index=equities&symbol=" + symbol + "&series=EQ&type=Dividends";
            String responseBody = makeRequest(url);
            if (responseBody != null) {
                records.addAll(parseDividendResponse(responseBody, symbol));
            }
        } catch (Exception e) {
            log.debug("NSE dividend fetch failed for {}: {}", symbol, e.getMessage());
        }
        return records;
    }

    private List<CorporateActionRecord> fetchCorporateActionsFromNse(String symbol, LocalDate from, LocalDate to) {
        List<CorporateActionRecord> records = new ArrayList<>();
        String[] types = {"Bonus", "Rights", "Stock Splits"};
        for (String type : types) {
            try {
                String url = corporateActionsUrl + "?index=equities&symbol=" + symbol + "&series=EQ&type=" + type;
                String responseBody = makeRequest(url);
                if (responseBody != null) {
                    records.addAll(parseNseCorporateActions(responseBody, symbol));
                }
            } catch (Exception e) {
                log.debug("NSE {} fetch failed for {}: {}", type, symbol, e.getMessage());
            }
        }
        return records;
    }

    private String makeRequest(String url) {
        try {
            HttpHeaders headers = buildHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.debug("NSE request failed: {}", e.getMessage());
        }
        return null;
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", userAgent);
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Accept-Language", "en-US,en;q=0.9");
        headers.set("Referer", nseBaseUrl + "/");
        headers.set("Connection", "keep-alive");
        return headers;
    }

    private List<CorporateActionRecord> parseDividendResponse(String json, String symbol) {
        List<CorporateActionRecord> records = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode data = root.isArray() ? root : root.get("data");
            if (data == null || !data.isArray()) return records;

            for (JsonNode item : data) {
                try {
                    String sym = item.has("symbol") ? item.get("symbol").asText() : symbol;
                    LocalDate exDate = parseDate(item.has("exDate") ? item.get("exDate").asText() :
                            item.has("ex_date") ? item.get("ex_date").asText() : null);
                    LocalDate recordDate = parseDate(item.has("recordDate") ? item.get("recordDate").asText() : null);
                    LocalDate paymentDate = parseDate(item.has("paymentDate") ? item.get("paymentDate").asText() : null);
                    LocalDate announcementDate = parseDate(item.has("bcStartDate") ? item.get("bcStartDate").asText() : null);

                    String amountStr = item.has("dividend") ? item.get("dividend").asText() :
                            item.has("amount") ? item.get("amount").asText() : "0";
                    BigDecimal amount = parseBigDecimal(amountStr);

                    String dividendType = item.has("purpose") ? item.get("purpose").asText() : "Dividend";
                    String recordId = sym + "_DIV_" + (exDate != null ? exDate : System.currentTimeMillis());

                    CorporateActionRecord record = CorporateActionRecord.builder()
                            .sourceRecordId(recordId)
                            .source("NSE")
                            .companySymbol(sym.toUpperCase())
                            .exchange("NSE")
                            .eventType(EventType.DIVIDEND)
                            .exDate(exDate)
                            .recordDate(recordDate)
                            .paymentDate(paymentDate)
                            .announcementDate(announcementDate)
                            .amount(amount)
                            .currency("INR")
                            .dividendType(dividendType)
                            .rawPayload(item.toString())
                            .build();
                    records.add(record);
                } catch (Exception e) {
                    log.debug("Failed to parse NSE dividend record: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse NSE dividend response: {}", e.getMessage());
        }
        return records;
    }

    private List<CorporateActionRecord> parseNseCorporateActions(String json, String symbol) {
        List<CorporateActionRecord> records = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode data = root.isArray() ? root : root.get("data");
            if (data == null || !data.isArray()) return records;

            for (JsonNode item : data) {
                try {
                    String sym = item.has("symbol") ? item.get("symbol").asText() :
                            (symbol != null ? symbol : "UNKNOWN");
                    String purpose = item.has("purpose") ? item.get("purpose").asText().toUpperCase() : "";
                    EventType eventType = detectEventType(purpose);

                    LocalDate exDate = parseDate(item.has("exDate") ? item.get("exDate").asText() : null);
                    LocalDate recordDate = parseDate(item.has("recordDate") ? item.get("recordDate").asText() : null);
                    LocalDate announcementDate = parseDate(item.has("bcStartDate") ? item.get("bcStartDate").asText() : null);

                    String recordId = sym + "_" + eventType + "_" + (exDate != null ? exDate : System.currentTimeMillis());

                    CorporateActionRecord.CorporateActionRecordBuilder builder = CorporateActionRecord.builder()
                            .sourceRecordId(recordId)
                            .source("NSE")
                            .companySymbol(sym.toUpperCase())
                            .exchange("NSE")
                            .eventType(eventType)
                            .exDate(exDate)
                            .recordDate(recordDate)
                            .announcementDate(announcementDate)
                            .currency("INR")
                            .description(purpose)
                            .rawPayload(item.toString());

                    // Parse ratio for splits/bonus
                    if (item.has("faceVal")) {
                        String faceVal = item.get("faceVal").asText();
                        parseRatio(faceVal, builder);
                    }

                    records.add(builder.build());
                } catch (Exception e) {
                    log.debug("Failed to parse NSE CA record: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse NSE CA response: {}", e.getMessage());
        }
        return records;
    }

    private EventType detectEventType(String purpose) {
        if (purpose.contains("DIVIDEND") || purpose.contains("DIV")) return EventType.DIVIDEND;
        if (purpose.contains("SPLIT") || purpose.contains("SUB-DIVISION")) return EventType.STOCK_SPLIT;
        if (purpose.contains("BONUS")) return EventType.BONUS_ISSUE;
        if (purpose.contains("RIGHTS")) return EventType.RIGHTS_ISSUE;
        if (purpose.contains("AGM")) return EventType.AGM;
        if (purpose.contains("EGM")) return EventType.EGM;
        if (purpose.contains("BUYBACK") || purpose.contains("BUY-BACK")) return EventType.BUYBACK;
        if (purpose.contains("RESULT") || purpose.contains("QUARTERLY") || purpose.contains("ANNUAL")) return EventType.EARNINGS;
        return EventType.OTHER;
    }

    private void parseRatio(String ratioStr, CorporateActionRecord.CorporateActionRecordBuilder builder) {
        try {
            if (ratioStr.contains(":")) {
                String[] parts = ratioStr.split(":");
                builder.ratioFrom(new BigDecimal(parts[0].trim()));
                builder.ratioTo(new BigDecimal(parts[1].trim()));
            }
        } catch (Exception e) {
            log.debug("Could not parse ratio: {}", ratioStr);
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank() || dateStr.equals("-")) return null;
        try {
            return LocalDate.parse(dateStr.trim(), NSE_DATE_FORMAT);
        } catch (DateTimeParseException e1) {
            try {
                return LocalDate.parse(dateStr.trim(), ALT_DATE_FORMAT);
            } catch (DateTimeParseException e2) {
                log.debug("Could not parse date: {}", dateStr);
                return null;
            }
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank() || value.equals("-")) return null;
        try {
            return new BigDecimal(value.replaceAll("[^0-9.]", ""));
        } catch (Exception e) {
            return null;
        }
    }
}
