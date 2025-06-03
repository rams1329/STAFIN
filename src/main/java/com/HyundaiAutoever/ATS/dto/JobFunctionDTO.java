package com.HyundaiAutoever.ATS.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobFunctionDTO {
    
    private Long id;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @Builder.Default
    private boolean active = true;
} 