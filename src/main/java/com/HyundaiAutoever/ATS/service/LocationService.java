package com.HyundaiAutoever.ATS.service;

import com.HyundaiAutoever.ATS.dto.LocationDTO;
import java.util.List;

public interface LocationService {
    
    // Admin operations
    LocationDTO createLocation(LocationDTO locationDTO);
    List<LocationDTO> getAllLocations();
    LocationDTO getLocationById(Long id);
    LocationDTO updateLocation(Long id, LocationDTO locationDTO);
    LocationDTO toggleLocationActive(Long id);
    void deleteLocation(Long id);
    
    // Public operations
    List<LocationDTO> getAllActiveLocations();
    
    // Get just the names for the job creation form
    List<String> getAllLocationNames();
} 