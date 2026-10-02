package com.corporateactions.watchlist.controller;

import com.corporateactions.auth.repository.UserRepository;
import com.corporateactions.common.dto.ApiResponse;
import com.corporateactions.common.exception.ResourceNotFoundException;
import com.corporateactions.watchlist.dto.WatchlistDto;
import com.corporateactions.watchlist.service.WatchlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/watchlist")
@RequiredArgsConstructor
@Tag(name = "Watchlist", description = "Manage company watchlist")
@SecurityRequirement(name = "bearerAuth")
public class WatchlistController {

    private final WatchlistService watchlistService;
    private final UserRepository userRepository;

    @Operation(summary = "Get user's watchlist")
    @GetMapping
    public ResponseEntity<ApiResponse<WatchlistDto>> getWatchlist(Authentication auth) {
        Long userId = getUserId(auth);
        return ResponseEntity.ok(ApiResponse.success("Watchlist retrieved",
                watchlistService.getWatchlist(userId)));
    }

    @Operation(summary = "Add company to watchlist")
    @PostMapping("/{companyId}")
    public ResponseEntity<ApiResponse<WatchlistDto>> addCompany(
            @PathVariable Long companyId, Authentication auth) {
        Long userId = getUserId(auth);
        return ResponseEntity.ok(ApiResponse.success("Company added to watchlist",
                watchlistService.addCompany(userId, companyId)));
    }

    @Operation(summary = "Remove company from watchlist")
    @DeleteMapping("/{companyId}")
    public ResponseEntity<ApiResponse<Void>> removeCompany(
            @PathVariable Long companyId, Authentication auth) {
        Long userId = getUserId(auth);
        watchlistService.removeCompany(userId, companyId);
        return ResponseEntity.ok(ApiResponse.success("Company removed from watchlist", null));
    }

    private Long getUserId(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
    }
}
