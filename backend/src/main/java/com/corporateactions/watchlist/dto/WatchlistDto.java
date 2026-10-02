package com.corporateactions.watchlist.dto;

import com.corporateactions.company.dto.CompanyDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistDto {
    private Long id;
    private String name;
    private String description;
    private List<WatchlistCompanyDto> companies;
    private int companyCount;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WatchlistCompanyDto {
        private Long id;
        private CompanyDto company;
        private OffsetDateTime addedAt;
    }
}
