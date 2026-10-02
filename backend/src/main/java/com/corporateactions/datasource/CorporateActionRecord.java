package com.corporateactions.datasource;

import com.corporateactions.corporateaction.entity.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorporateActionRecord {
    private String sourceRecordId;
    private String source;
    private String companySymbol;
    private String companyIsin;
    private String companyName;
    private String exchange;

    private EventType eventType;
    private LocalDate announcementDate;
    private LocalDate exDate;
    private LocalDate recordDate;
    private LocalDate paymentDate;
    private LocalDate effectiveDate;

    private BigDecimal amount;
    private String currency;
    private BigDecimal ratioFrom;
    private BigDecimal ratioTo;
    private BigDecimal issuePrice;
    private LocalDate subscriptionStartDate;
    private LocalDate subscriptionEndDate;

    private String quarter;
    private String financialYear;
    private String meetingType;
    private BigDecimal buybackPrice;
    private Long buybackQuantity;
    private LocalDate periodStartDate;
    private LocalDate periodEndDate;
    private String dividendType;
    private String description;

    @Builder.Default
    private Map<String, String> additionalAttributes = new HashMap<>();

    private String rawPayload;
}
