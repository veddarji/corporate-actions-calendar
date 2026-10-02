package com.corporateactions.corporateaction.dto;

import com.corporateactions.corporateaction.entity.CorporateActionStatus;
import com.corporateactions.corporateaction.entity.DataQualityStatus;
import com.corporateactions.corporateaction.entity.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorporateActionDto {
    private Long id;
    private Long companyId;
    private String companyName;
    private String companySymbol;
    private String companyExchange;

    private EventType eventType;
    private String eventTypeLabel;

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

    private CorporateActionStatus status;
    private DataQualityStatus dataQuality;
    private String source;
    private String sourceReference;

    private OffsetDateTime sourceUpdatedAt;
    private OffsetDateTime lastVerifiedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // Computed helper for primary display date
    public LocalDate getPrimaryDate() {
        if (effectiveDate != null) return effectiveDate;
        if (exDate != null) return exDate;
        if (recordDate != null) return recordDate;
        if (paymentDate != null) return paymentDate;
        return announcementDate;
    }
}
