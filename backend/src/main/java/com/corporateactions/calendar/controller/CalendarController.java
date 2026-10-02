package com.corporateactions.calendar.controller;

import com.corporateactions.auth.repository.UserRepository;
import com.corporateactions.calendar.dto.CalendarResponse;
import com.corporateactions.common.dto.ApiResponse;
import com.corporateactions.common.exception.ResourceNotFoundException;
import com.corporateactions.corporateaction.dto.CorporateActionDto;
import com.corporateactions.corporateaction.service.CorporateActionService;
import com.corporateactions.watchlist.service.WatchlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/calendar")
@RequiredArgsConstructor
@Tag(name = "Calendar", description = "Corporate events calendar - reads pre-computed data only")
@SecurityRequirement(name = "bearerAuth")
public class CalendarController {

    private final CorporateActionService corporateActionService;
    private final WatchlistService watchlistService;
    private final UserRepository userRepository;

    /**
     * Get calendar events for authenticated user's watchlist.
     * Reads from DB/Redis only - NEVER calls external APIs.
     */
    @Operation(summary = "Get calendar events for watched companies")
    @GetMapping
    public ResponseEntity<ApiResponse<CalendarResponse>> getCalendar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication auth) {

        Long userId = getUserId(auth);
        LocalDate start = from != null ? from : LocalDate.now().withDayOfMonth(1);
        LocalDate end = to != null ? to : start.plusMonths(3);

        List<Long> companyIds = watchlistService.getWatchedCompanyIds(userId);
        List<CorporateActionDto> events = corporateActionService.getCalendarEvents(companyIds, start, end);

        CalendarResponse response = CalendarResponse.builder()
                .from(start)
                .to(end)
                .totalEvents(events.size())
                .events(events)
                .eventsByDate(groupByDate(events))
                .build();

        return ResponseEntity.ok(ApiResponse.success("Calendar data retrieved", response));
    }

    @Operation(summary = "Get upcoming events (next 30 days)")
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<CorporateActionDto>>> getUpcoming(Authentication auth) {
        Long userId = getUserId(auth);
        List<Long> companyIds = watchlistService.getWatchedCompanyIds(userId);
        List<CorporateActionDto> events = corporateActionService.getUpcoming(companyIds);
        return ResponseEntity.ok(ApiResponse.success("Upcoming events retrieved", events));
    }

    @Operation(summary = "Get events for a specific company")
    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<List<CorporateActionDto>>> getByCompany(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var paged = corporateActionService.getByCompany(companyId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Company events retrieved", paged.getContent()));
    }

    private Long getUserId(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
    }

    private Map<String, List<CorporateActionDto>> groupByDate(List<CorporateActionDto> events) {
        return events.stream().collect(Collectors.groupingBy(e -> {
            LocalDate date = e.getPrimaryDate();
            return date != null ? date.toString() : "unknown";
        }));
    }
}
