package com.corporateactions.watchlist.repository;

import com.corporateactions.watchlist.entity.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    @Query("SELECT w FROM Watchlist w LEFT JOIN FETCH w.companies wc LEFT JOIN FETCH wc.company WHERE w.user.id = :userId")
    Optional<Watchlist> findByUserIdWithCompanies(@Param("userId") Long userId);

    Optional<Watchlist> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
