package com.corporateactions.company.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDto {
    private Long id;
    private String symbol;
    private String exchange;
    private String isin;
    private String name;
    private String sector;
    private String industry;
    private String currency;
    private String country;
    private Boolean active;
    private OffsetDateTime lastSyncedAt;
    private OffsetDateTime createdAt;
}
