package com.corporateactions.ingestion.repository;

import com.corporateactions.ingestion.entity.IngestionJobLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IngestionJobLogRepository extends JpaRepository<IngestionJobLog, Long> {

    Page<IngestionJobLog> findAllByOrderByStartedAtDesc(Pageable pageable);

    @Query("SELECT j FROM IngestionJobLog j WHERE j.status = 'COMPLETED' ORDER BY j.completedAt DESC")
    Optional<IngestionJobLog> findLastSuccessful();

    long countByStatus(IngestionJobLog.JobStatus status);
}
