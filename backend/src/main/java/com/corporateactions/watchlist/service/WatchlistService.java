package com.corporateactions.watchlist.service;

import com.corporateactions.auth.entity.User;
import com.corporateactions.auth.repository.UserRepository;
import com.corporateactions.common.exception.BusinessException;
import com.corporateactions.common.exception.ResourceNotFoundException;
import com.corporateactions.company.entity.Company;
import com.corporateactions.company.repository.CompanyRepository;
import com.corporateactions.company.service.CompanyService;
import com.corporateactions.watchlist.dto.WatchlistDto;
import com.corporateactions.watchlist.entity.Watchlist;
import com.corporateactions.watchlist.entity.WatchlistCompany;
import com.corporateactions.watchlist.repository.WatchlistCompanyRepository;
import com.corporateactions.watchlist.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final WatchlistCompanyRepository watchlistCompanyRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CompanyService companyService;

    @Transactional(readOnly = true)
    public WatchlistDto getWatchlist(Long userId) {
        Watchlist watchlist = getOrCreateWatchlist(userId);
        return toDto(watchlist);
    }

    /**
     * Add a company to user's watchlist.
     * NEVER calls external APIs - only operates on local DB data.
     */
    @Transactional
    public WatchlistDto addCompany(Long userId, Long companyId) {
        Watchlist watchlist = getOrCreateWatchlist(userId);

        if (watchlistCompanyRepository.existsByWatchlistIdAndCompanyId(watchlist.getId(), companyId)) {
            log.info("Company {} already in watchlist {}, returning existing", companyId, watchlist.getId());
            return toDto(watchlist);
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + companyId));

        WatchlistCompany entry = WatchlistCompany.builder()
                .watchlist(watchlist)
                .company(company)
                .build();
        watchlistCompanyRepository.save(entry);
        watchlist.getCompanies().add(entry);

        log.info("User {} added company {} to watchlist", userId, company.getSymbol());
        return toDto(watchlistRepository.findByUserIdWithCompanies(userId).orElse(watchlist));
    }

    @Transactional
    public void removeCompany(Long userId, Long companyId) {
        Watchlist watchlist = watchlistRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Watchlist not found"));

        if (!watchlistCompanyRepository.existsByWatchlistIdAndCompanyId(watchlist.getId(), companyId)) {
            throw new ResourceNotFoundException("Company not in watchlist");
        }

        watchlistCompanyRepository.deleteByWatchlistIdAndCompanyId(watchlist.getId(), companyId);
        log.info("User {} removed company {} from watchlist", userId, companyId);
    }

    public List<Long> getWatchedCompanyIds(Long userId) {
        return watchlistCompanyRepository.findCompanyIdsByUserId(userId);
    }

    private Watchlist getOrCreateWatchlist(Long userId) {
        return watchlistRepository.findByUserIdWithCompanies(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                    Watchlist w = Watchlist.builder().user(user).build();
                    return watchlistRepository.save(w);
                });
    }

    private WatchlistDto toDto(Watchlist w) {
        List<WatchlistDto.WatchlistCompanyDto> companiesDto = w.getCompanies().stream()
                .map(wc -> WatchlistDto.WatchlistCompanyDto.builder()
                        .id(wc.getId())
                        .company(companyService.toDto(wc.getCompany()))
                        .addedAt(wc.getAddedAt())
                        .build())
                .collect(Collectors.toList());

        return WatchlistDto.builder()
                .id(w.getId())
                .name(w.getName())
                .description(w.getDescription())
                .companies(companiesDto)
                .companyCount(companiesDto.size())
                .createdAt(w.getCreatedAt())
                .updatedAt(w.getUpdatedAt())
                .build();
    }
}
