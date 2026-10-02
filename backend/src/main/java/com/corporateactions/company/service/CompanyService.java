package com.corporateactions.company.service;

import com.corporateactions.common.exception.ResourceNotFoundException;
import com.corporateactions.company.dto.CompanyDto;
import com.corporateactions.company.entity.Company;
import com.corporateactions.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Cacheable(value = "company-search", key = "#query + '-' + #page + '-' + #size")
    public Page<CompanyDto> searchCompanies(String query, int page, int size) {
        if (query == null || query.trim().length() < 1) {
            return companyRepository.findAll(
                    PageRequest.of(page, size, Sort.by("name")))
                    .map(this::toDto);
        }
        return companyRepository.searchCompanies(
                query.trim(),
                PageRequest.of(page, size, Sort.by("name")))
                .map(this::toDto);
    }

    @Cacheable(value = "company-details", key = "#id")
    public CompanyDto getById(Long id) {
        return companyRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + id));
    }

    public CompanyDto getBySymbol(String exchange, String symbol) {
        return companyRepository.findByExchangeAndSymbol(exchange, symbol)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + exchange + ":" + symbol));
    }

    @Transactional
    @CacheEvict(value = {"company-details", "company-search"}, allEntries = true)
    public CompanyDto save(Company company) {
        Company saved = companyRepository.save(company);
        return toDto(saved);
    }

    public List<CompanyDto> getByIds(List<Long> ids) {
        return companyRepository.findByIdIn(ids)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public CompanyDto toDto(Company c) {
        return CompanyDto.builder()
                .id(c.getId())
                .symbol(c.getSymbol())
                .exchange(c.getExchange())
                .isin(c.getIsin())
                .name(c.getName())
                .sector(c.getSector())
                .industry(c.getIndustry())
                .currency(c.getCurrency())
                .country(c.getCountry())
                .active(c.getActive())
                .lastSyncedAt(c.getLastSyncedAt())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
