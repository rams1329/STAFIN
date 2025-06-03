package com.HyundaiAutoever.ATS.controller;

import com.HyundaiAutoever.ATS.dto.ApiResponse;
import com.HyundaiAutoever.ATS.dto.LocationDTO;
import com.HyundaiAutoever.ATS.entity.Location;
import com.HyundaiAutoever.ATS.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    // Public endpoint for active locations (no authentication required)
    @GetMapping("/public/locations/active")
    public ResponseEntity<ApiResponse<List<LocationDTO>>> getPublicActiveLocations() {
        List<LocationDTO> locations = locationService.getAllActiveLocations();
        return ResponseEntity.ok(ApiResponse.success("Active locations retrieved successfully", locations));
    }

    // Admin endpoint for all locations
    @GetMapping("/locations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LocationDTO>> getAllLocations() {
        return ResponseEntity.ok(locationService.getAllLocations());
    }
    
    // Admin endpoint for active locations - alternative URL
    @GetMapping("/locations/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LocationDTO>> getActiveLocations() {
        return ResponseEntity.ok(locationService.getAllActiveLocations());
    }
    
    // Admin endpoint to get all location names for job postings
    @GetMapping("/job-locations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> getAllLocationNames() {
        return ResponseEntity.ok(locationService.getAllLocationNames());
    }
    
    // Admin endpoint to get location by ID
    @GetMapping("/locations/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocationDTO> getLocationById(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.getLocationById(id));
    }
    
    // Admin endpoint to create location
    @PostMapping("/locations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LocationDTO>> createLocation(@Valid @RequestBody LocationDTO locationDTO) {
        try {
            LocationDTO createdLocation = locationService.createLocation(locationDTO);
            return ResponseEntity.ok(ApiResponse.success("Location created successfully", createdLocation));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create location: " + e.getMessage()));
        }
    }
    
    // Admin endpoint to update location
    @PutMapping("/locations/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LocationDTO>> updateLocation(
            @PathVariable Long id, 
            @Valid @RequestBody LocationDTO locationDTO) {
        try {
            LocationDTO updatedLocation = locationService.updateLocation(id, locationDTO);
            return ResponseEntity.ok(ApiResponse.success("Location updated successfully", updatedLocation));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to update location: " + e.getMessage()));
        }
    }
    
    // Admin endpoint to delete location
    @DeleteMapping("/locations/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteLocation(@PathVariable Long id) {
        try {
            locationService.deleteLocation(id);
            return ResponseEntity.ok(ApiResponse.success("Location deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete location: " + e.getMessage()));
        }
    }
    
    // Admin endpoint to toggle location active status
    @PutMapping("/locations/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LocationDTO>> toggleLocationActive(@PathVariable Long id) {
        try {
            LocationDTO updatedLocation = locationService.toggleLocationActive(id);
            String status = updatedLocation.getIsActive() ? "activated" : "deactivated";
            return ResponseEntity.ok(ApiResponse.success("Location " + status + " successfully", updatedLocation));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to toggle location status: " + e.getMessage()));
        }
    }
}