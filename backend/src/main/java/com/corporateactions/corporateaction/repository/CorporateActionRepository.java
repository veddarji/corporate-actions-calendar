package com.corporateactions.corporateaction.repository;

import com.corporateactions.corporateaction.entity.CorporateAction;
import com.corporateactions.corporateaction.entity.DataQualityStatus;
import com.corporateactions.corporateaction.entity.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CorporateActionRepository extends JpaRepository<CorporateAction, Long> {

    // For calendar queries - company + date range
    @Query("SELECT ca FROM CorporateAction ca JOIN FETCH ca.company " +
           "WHERE ca.company.id IN :companyIds " +
           "AND (ca.exDate BETWEEN :from AND :to " +
           "  OR ca.recordDate BETWEEN :from AND :to " +
           "  OR ca.paymentDate BETWEEN :from AND :to " +
           "  OR ca.effectiveDate BETWEEN :from AND :to " +
           "  OR ca.announcementDate BETWEEN :from AND :to) " +
           "ORDER BY COALESCE(ca.effectiveDate, ca.exDate, ca.recordDate, ca.announcementDate) ASC")
    List<CorporateAction> findByCompanyIdsAndDateRange(
            @Param("companyIds") List<Long> companyIds,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("SELECT ca FROM CorporateAction ca JOIN FETCH ca.company " +
           "WHERE ca.company.id = :companyId " +
           "ORDER BY COALESCE(ca.effectiveDate, ca.exDate, ca.recordDate, ca.announcementDate) DESC")
    Page<CorporateAction> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);

    // Upcoming events
    @Query("SELECT ca FROM CorporateAction ca JOIN FETCH ca.company " +
           "WHERE ca.company.id IN :companyIds " +
           "AND (ca.exDate >= :today OR ca.recordDate >= :today OR ca.paymentDate >= :today OR ca.effectiveDate >= :today) " +
           "ORDER BY COALESCE(ca.effectiveDate, ca.exDate, ca.recordDate, ca.paymentDate) ASC")
    List<CorporateAction> findUpcoming(
            @Param("companyIds") List<Long> companyIds,
            @Param("today") LocalDate today);

    // For deduplication
    Optional<CorporateAction> findBySourceAndSourceRecordId(String source, String sourceRecordId);

    // Admin - conflict detection
    List<CorporateAction> findByDataQuality(DataQualityStatus dataQuality);

    // Events by type
    @Query("SELECT ca FROM CorporateAction ca JOIN FETCH ca.company " +
           "WHERE ca.company.id IN :companyIds AND ca.eventType = :eventType " +
           "AND ca.exDate BETWEEN :from AND :to")
    List<CorporateAction> findByCompanyIdsAndEventTypeAndDateRange(
            @Param("companyIds") List<Long> companyIds,
            @Param("eventType") EventType eventType,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    long countByDataQuality(DataQualityStatus dataQuality);

    long countByCompanyId(Long companyId);
}
