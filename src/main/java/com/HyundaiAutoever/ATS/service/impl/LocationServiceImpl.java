package com.HyundaiAutoever.ATS.service.impl;

import com.HyundaiAutoever.ATS.dto.LocationDTO;
import com.HyundaiAutoever.ATS.entity.Location;
import com.HyundaiAutoever.ATS.exception.ResourceNotFoundException;
import com.HyundaiAutoever.ATS.repository.LocationRepository;
import com.HyundaiAutoever.ATS.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationServiceImpl implements LocationService {
    
    @Autowired
    private LocationRepository locationRepository;

    @Override
    @CacheEvict(value = {"activeLocations", "allLocations", "locationNames"}, allEntries = true)
    public LocationDTO createLocation(LocationDTO locationDTO) {
        Location location = new Location();
        location.setName(locationDTO.getName());
        location.setActive(locationDTO.getIsActive() != null ? locationDTO.getIsActive() : true);
        
        return mapToDTO(locationRepository.save(location));
    }

    @Override
    @Cacheable(value = "allLocations")
    public List<LocationDTO> getAllLocations() {
        List<Location> locations = locationRepository.findAll();
        return locations.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LocationDTO getLocationById(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", id));
        return mapToDTO(location);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "activeLocations", allEntries = true),
        @CacheEvict(value = "allLocations", allEntries = true),
        @CacheEvict(value = "locationNames", allEntries = true)
    })
    public LocationDTO updateLocation(Long id, LocationDTO locationDTO) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", id));
        
        location.setName(locationDTO.getName());
        if (locationDTO.getIsActive() != null) {
            location.setActive(locationDTO.getIsActive());
        }
        
        return mapToDTO(locationRepository.save(location));
    }

    @Override
    @CacheEvict(value = {"activeLocations", "allLocations", "locationNames"}, allEntries = true)
    public void deleteLocation(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", id));
        locationRepository.delete(location);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "activeLocations", allEntries = true),
        @CacheEvict(value = "allLocations", allEntries = true),
        @CacheEvict(value = "locationNames", allEntries = true)
    })
    public LocationDTO toggleLocationActive(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", id));
        
        // Toggle the active status
        location.setActive(!location.isActive());
        
        return mapToDTO(locationRepository.save(location));
    }

    @Override
    @Cacheable(value = "activeLocations")
    public List<LocationDTO> getAllActiveLocations() {
        List<Location> locations = locationRepository.findByIsActiveTrue();
        return locations.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable(value = "locationNames")
    public List<String> getAllLocationNames() {
        // If no locations exist, return default empty list
        if (locationRepository.count() == 0) {
            return new ArrayList<>();
        }
        
        // Otherwise return all active location names
        return locationRepository.findByIsActiveTrue().stream()
                .map(Location::getName)
                .collect(Collectors.toList());
    }

    private LocationDTO mapToDTO(Location location) {
        LocationDTO dto = new LocationDTO();
        dto.setId(location.getId());
        dto.setName(location.getName());
        dto.setIsActive(location.isActive());
        return dto;
    }
} 