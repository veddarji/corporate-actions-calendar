package com.corporateactions.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDto {
    private long totalCompanies;
    private long totalCorporateActions;
    private long pendingReviewCount;
    private long conflictCount;
    private long invalidCount;
    private long totalUsers;
    private long jobsCompleted;
    private long jobsFailed;
    private OffsetDateTime lastIngestionAt;
    private String lastIngestionStatus;
    private boolean anySourceAvailable;
}
