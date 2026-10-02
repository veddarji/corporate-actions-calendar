package com.corporateactions.auth.dto;

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
public class UserProfileDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private Boolean emailVerified;
    private String timezone;
    private List<String> roles;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastLoginAt;
}
