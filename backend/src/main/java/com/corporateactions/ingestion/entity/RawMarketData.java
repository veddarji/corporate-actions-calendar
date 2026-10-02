package com.corporateactions.ingestion.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Entity
@Table(name = "raw_market_data")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawMarketData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String source;

    @Column(name = "source_record_id", length = 255)
    private String sourceRecordId;

    @Column(name = "company_symbol", length = 50)
    private String companySymbol;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json", nullable = false, columnDefinition = "JSONB")
    private String payloadJson;

    @CreationTimestamp
    @Column(name = "retrieved_at", nullable = false, updatable = false)
    private OffsetDateTime retrievedAt;

    @Column(nullable = false, length = 64)
    private String checksum;

    @Column(nullable = false)
    @Builder.Default
    private Boolean processed = false;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @Column(name = "processing_error", columnDefinition = "TEXT")
    private String processingError;
}
