package com.corporateactions.watchlist.repository;

import com.corporateactions.watchlist.entity.WatchlistCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchlistCompanyRepository extends JpaRepository<WatchlistCompany, Long> {

    Optional<WatchlistCompany> findByWatchlistIdAndCompanyId(Long watchlistId, Long companyId);

    boolean existsByWatchlistIdAndCompanyId(Long watchlistId, Long companyId);

    @Query("SELECT wc.company.id FROM WatchlistCompany wc WHERE wc.watchlist.user.id = :userId")
    List<Long> findCompanyIdsByUserId(@Param("userId") Long userId);

    void deleteByWatchlistIdAndCompanyId(Long watchlistId, Long companyId);
}
