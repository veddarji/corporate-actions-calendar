package com.corporateactions.company.controller;

import com.corporateactions.common.dto.ApiResponse;
import com.corporateactions.common.dto.PagedResponse;
import com.corporateactions.company.dto.CompanyDto;
import com.corporateactions.company.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Companies", description = "Company search and details")
public class CompanyController {

    private final CompanyService companyService;

    @Operation(summary = "Search companies by name, symbol, or ISIN (public)")
    @GetMapping("/api/v1/public/companies/search")
    public ResponseEntity<ApiResponse<PagedResponse<CompanyDto>>> publicSearch(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<CompanyDto> result = companyService.searchCompanies(q, page, Math.min(size, 50));
        return ResponseEntity.ok(ApiResponse.success("Companies found",
                PagedResponse.of(result)));
    }

    @Operation(summary = "Search companies (authenticated)")
    @GetMapping("/api/v1/companies/search")
    public ResponseEntity<ApiResponse<PagedResponse<CompanyDto>>> search(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<CompanyDto> result = companyService.searchCompanies(q, page, Math.min(size, 50));
        return ResponseEntity.ok(ApiResponse.success("Companies found",
                PagedResponse.of(result)));
    }

    @Operation(summary = "Get company by ID")
    @GetMapping("/api/v1/companies/{id}")
    public ResponseEntity<ApiResponse<CompanyDto>> getById(@PathVariable Long id) {
        CompanyDto company = companyService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Company found", company));
    }
}
