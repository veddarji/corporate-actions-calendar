package com.corporateactions.corporateaction.entity;

import com.corporateactions.company.entity.Company;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "corporate_actions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class CorporateAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "event_type", nullable = false, columnDefinition = "event_type")
    private EventType eventType;

    @Column(name = "announcement_date")
    private LocalDate announcementDate;

    @Column(name = "ex_date")
    private LocalDate exDate;

    @Column(name = "record_date")
    private LocalDate recordDate;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(precision = 20, scale = 6)
    private BigDecimal amount;

    @Column(length = 10)
    @Builder.Default
    private String currency = "INR";

    @Column(name = "ratio_from", precision = 10, scale = 4)
    private BigDecimal ratioFrom;

    @Column(name = "ratio_to", precision = 10, scale = 4)
    private BigDecimal ratioTo;

    @Column(name = "issue_price", precision = 20, scale = 6)
    private BigDecimal issuePrice;

    @Column(name = "subscription_start_date")
    private LocalDate subscriptionStartDate;

    @Column(name = "subscription_end_date")
    private LocalDate subscriptionEndDate;

    @Column(length = 10)
    private String quarter;

    @Column(name = "financial_year", length = 10)
    private String financialYear;

    @Column(name = "meeting_type", length = 20)
    private String meetingType;

    @Column(name = "buyback_price", precision = 20, scale = 6)
    private BigDecimal buybackPrice;

    @Column(name = "buyback_quantity")
    private Long buybackQuantity;

    @Column(name = "period_start_date")
    private LocalDate periodStartDate;

    @Column(name = "period_end_date")
    private LocalDate periodEndDate;

    @Column(name = "dividend_type", length = 50)
    private String dividendType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "corporate_action_status")
    @Builder.Default
    private CorporateActionStatus status = CorporateActionStatus.ANNOUNCED;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "data_quality", nullable = false, columnDefinition = "data_quality_status")
    @Builder.Default
    private DataQualityStatus dataQuality = DataQualityStatus.PENDING_REVIEW;

    @Column(nullable = false, length = 50)
    private String source;

    @Column(name = "source_reference", length = 255)
    private String sourceReference;

    @Column(name = "source_record_id", length = 255)
    private String sourceRecordId;

    @Column(name = "source_updated_at")
    private OffsetDateTime sourceUpdatedAt;

    @Column(name = "last_verified_at")
    private OffsetDateTime lastVerifiedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "corporateAction", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CorporateActionAttribute> attributes = new ArrayList<>();
}
