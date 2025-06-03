package com.HyundaiAutoever.ATS.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuRoleAssignmentRequest {
    
    @NotNull(message = "Role ID is required")
    private Long roleId;
    
    @NotEmpty(message = "Menu IDs list cannot be empty")
    private List<Long> menuIds;
    
    @Builder.Default
    private boolean canView = true;
    
    @Builder.Default
    private boolean canAdd = false;
    
    @Builder.Default
    private boolean canEdit = false;
    
    @Builder.Default
    private boolean canDelete = false;
} 