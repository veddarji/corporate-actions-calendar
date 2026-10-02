package com.corporateactions.company.repository;

import com.corporateactions.company.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByExchangeAndSymbol(String exchange, String symbol);

    Optional<Company> findByIsin(String isin);

    @Query("SELECT c FROM Company c WHERE c.active = true AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.symbol) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.isin) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Company> searchCompanies(@Param("query") String query, Pageable pageable);

    @Query("SELECT c FROM Company c WHERE c.active = true ORDER BY c.name")
    List<Company> findAllActive();

    List<Company> findByIdIn(List<Long> ids);

    boolean existsByExchangeAndSymbol(String exchange, String symbol);
}
