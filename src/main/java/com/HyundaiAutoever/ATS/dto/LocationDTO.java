package com.HyundaiAutoever.ATS.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDTO {
    private Long id;
    
    @NotBlank(message = "Location name must not be blank")
    private String name;
    
    private Boolean isActive;
} 