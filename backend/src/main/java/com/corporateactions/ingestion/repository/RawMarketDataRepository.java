package com.corporateactions.ingestion.repository;

import com.corporateactions.ingestion.entity.RawMarketData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RawMarketDataRepository extends JpaRepository<RawMarketData, Long> {
    Optional<RawMarketData> findBySourceAndChecksum(String source, String checksum);
    boolean existsBySourceAndChecksum(String source, String checksum);
}
