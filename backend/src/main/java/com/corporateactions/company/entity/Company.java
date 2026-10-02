package com.corporateactions.company.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "companies",
        uniqueConstraints = @UniqueConstraint(columnNames = {"exchange", "symbol"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String symbol;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String exchange = "NSE";

    @Column(length = 12)
    private String isin;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 100)
    private String sector;

    @Column(length = 100)
    private String industry;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "INR";

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String country = "India";

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "last_synced_at")
    private OffsetDateTime lastSyncedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
