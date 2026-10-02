package com.corporateactions.datasource;

import java.time.LocalDate;
import java.util.List;

/**
 * Abstraction for corporate action data sources.
 * All sources must implement this interface to plug into the ingestion pipeline.
 */
public interface CorporateActionDataSource {

    /**
     * @return unique name for this data source (e.g., "NSE", "YAHOO_FINANCE", "MOCK")
     */
    String getSourceName();

    /**
     * @return whether this source is currently available
     */
    boolean isAvailable();

    /**
     * Fetch corporate actions for a company in a date range.
     * Implementations must handle retries, rate limiting, and errors internally.
     */
    List<CorporateActionRecord> fetchCorporateActions(String companySymbol, LocalDate from, LocalDate to);

    /**
     * Fetch all recent corporate actions (no company filter).
     * Not all sources support this - return empty list if not supported.
     */
    default List<CorporateActionRecord> fetchAllRecentCorporateActions(LocalDate from, LocalDate to) {
        return List.of();
    }

    /**
     * Priority order when multiple sources are available (lower = higher priority)
     */
    default int getPriority() {
        return 100;
    }
}
