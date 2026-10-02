package com.corporateactions.calendar.dto;

import com.corporateactions.corporateaction.dto.CorporateActionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarResponse {
    private LocalDate from;
    private LocalDate to;
    private int totalEvents;
    private List<CorporateActionDto> events;
    private Map<String, List<CorporateActionDto>> eventsByDate;
}
