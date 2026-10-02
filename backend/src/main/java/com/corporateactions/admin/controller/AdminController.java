package com.corporateactions.admin.controller;

import com.corporateactions.admin.dto.AdminDashboardDto;
import com.corporateactions.auth.repository.UserRepository;
import com.corporateactions.common.dto.ApiResponse;
import com.corporateactions.common.dto.PagedResponse;
import com.corporateactions.company.dto.CompanyDto;
import com.corporateactions.company.entity.Company;
import com.corporateactions.company.repository.CompanyRepository;
import com.corporateactions.company.service.CompanyService;
import com.corporateactions.corporateaction.dto.CorporateActionDto;
import com.corporateactions.corporateaction.entity.DataQualityStatus;
import com.corporateactions.corporateaction.repository.CorporateActionRepository;
import com.corporateactions.corporateaction.service.CorporateActionService;
import com.corporateactions.datasource.CorporateActionDataSource;
import com.corporateactions.ingestion.entity.IngestionJobLog;
import com.corporateactions.ingestion.repository.IngestionJobLogRepository;
import com.corporateactions.ingestion.scheduler.CorporateActionIngestionScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin operations - requires ADMIN or DATA_ADMIN role")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN') or hasRole('DATA_ADMIN')")
public class AdminController {

    private final CorporateActionRepository corporateActionRepository;
    private final IngestionJobLogRepository jobLogRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CorporateActionService corporateActionService;
    private final CompanyService companyService;
    private final CorporateActionIngestionScheduler scheduler;
    private final List<CorporateActionDataSource> dataSources;

    @Operation(summary = "Get admin dashboard metrics")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardDto>> getDashboard() {
        IngestionJobLog lastJob = jobLogRepository.findLastSuccessful().orElse(null);

        AdminDashboardDto dashboard = AdminDashboardDto.builder()
                .totalCompanies(companyRepository.count())
                .totalCorporateActions(corporateActionRepository.count())
                .pendingReviewCount(corporateActionRepository.countByDataQuality(DataQualityStatus.PENDING_REVIEW))
                .conflictCount(corporateActionRepository.countByDataQuality(DataQualityStatus.CONFLICT))
                .invalidCount(corporateActionRepository.countByDataQuality(DataQualityStatus.INVALID))
                .totalUsers(userRepository.count())
                .jobsCompleted(jobLogRepository.countByStatus(IngestionJobLog.JobStatus.COMPLETED))
                .jobsFailed(jobLogRepository.countByStatus(IngestionJobLog.JobStatus.FAILED))
                .lastIngestionAt(lastJob != null ? lastJob.getCompletedAt() : null)
                .lastIngestionStatus(lastJob != null ? lastJob.getStatus().name() : "NEVER_RAN")
                .anySourceAvailable(dataSources.stream().anyMatch(CorporateActionDataSource::isAvailable))
                .build();

        return ResponseEntity.ok(ApiResponse.success("Dashboard data retrieved", dashboard));
    }

    @Operation(summary = "Get ingestion job history")
    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<PagedResponse<IngestionJobLog>>> getJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<IngestionJobLog> jobs = jobLogRepository.findAllByOrderByStartedAtDesc(
                PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success("Jobs retrieved", PagedResponse.of(jobs)));
    }

    @Operation(summary = "Get events with CONFLICT data quality status")
    @GetMapping("/conflicts")
    public ResponseEntity<ApiResponse<List<CorporateActionDto>>> getConflicts() {
        List<CorporateActionDto> conflicts = corporateActionRepository
                .findByDataQuality(DataQualityStatus.CONFLICT)
                .stream()
                .map(corporateActionService::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Conflicts retrieved", conflicts));
    }

    @Operation(summary = "Get all companies (admin view)")
    @GetMapping("/companies")
    public ResponseEntity<ApiResponse<PagedResponse<CompanyDto>>> getCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<CompanyDto> companies = companyRepository
                .findAll(PageRequest.of(page, size, Sort.by("name")))
                .map(companyService::toDto);
        return ResponseEntity.ok(ApiResponse.success("Companies retrieved", PagedResponse.of(companies)));
    }

    @Operation(summary = "Add a new company to track")
    @PostMapping("/companies")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DATA_ADMIN')")
    public ResponseEntity<ApiResponse<CompanyDto>> addCompany(@RequestBody CompanyDto dto) {
        Company company = Company.builder()
                .symbol(dto.getSymbol().toUpperCase())
                .exchange(dto.getExchange() != null ? dto.getExchange() : "NSE")
                .isin(dto.getIsin())
                .name(dto.getName())
                .sector(dto.getSector())
                .industry(dto.getIndustry())
                .currency(dto.getCurrency() != null ? dto.getCurrency() : "INR")
                .country(dto.getCountry() != null ? dto.getCountry() : "India")
                .build();
        CompanyDto saved = companyService.save(company);
        return ResponseEntity.ok(ApiResponse.success("Company added", saved));
    }

    @Operation(summary = "Manually trigger an ingestion run")
    @PostMapping("/ingestion/trigger")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> triggerIngestion() {
        scheduler.triggerManualIngestion();
        return ResponseEntity.ok(ApiResponse.success("Ingestion triggered", "Running in background"));
    }
}
