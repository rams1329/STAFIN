package com.HyundaiAutoever.ATS.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JwtResponse {

    private String token;
    @Builder.Default    private String type = "Bearer";
    private Long id;
    private String name;
    private String email;
    private List<String> roles;
    private String role;
    private boolean isAdmin;
    private List<MenuDto> menus;
    private boolean firstLogin;
} 