package com.corporateactions.ingestion.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ingestion_job_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngestionJobLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_name", nullable = false, length = 100)
    private String jobName;

    @Column(nullable = false, length = 50)
    private String source;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "job_status")
    private JobStatus status;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "records_fetched")
    private Integer recordsFetched;

    @Column(name = "records_processed")
    private Integer recordsProcessed;

    @Column(name = "records_inserted")
    private Integer recordsInserted;

    @Column(name = "records_updated")
    private Integer recordsUpdated;

    @Column(name = "records_skipped")
    private Integer recordsSkipped;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    public enum JobStatus {
        RUNNING, COMPLETED, FAILED, SKIPPED
    }
}
