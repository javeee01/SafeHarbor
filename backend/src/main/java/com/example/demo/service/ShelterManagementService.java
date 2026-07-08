package com.example.demo.service;

import com.example.demo.dto.ReliefShelterRequestDto;
import com.example.demo.dto.ReliefShelterResponseDto;
import com.example.demo.entity.ReliefShelter;
import com.example.demo.exception.BusinessValidationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ReliefShelterRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShelterManagementService {
    private final ReliefShelterRepository shelterRepository;

    public ShelterManagementService(ReliefShelterRepository shelterRepository) {
        this.shelterRepository = shelterRepository;
    }

    @Transactional
    public ReliefShelterResponseDto registerShelter(ReliefShelterRequestDto dto) {
        ReliefShelter shelter = new ReliefShelter();
        applyFields(shelter, dto);
        shelter.setActive(true);
        return toResponse(shelterRepository.save(shelter));
    }

    @Transactional
    public ReliefShelterResponseDto adjustOccupancy(Long id, int intakeCount) {
        ReliefShelter shelter = findShelter(id);
        int newOccupancy = shelter.getCurrentOccupancy() + intakeCount;
        if (newOccupancy < 0) {
            throw new BusinessValidationException("Shelter occupancy cannot be negative");
        }
        if (newOccupancy > shelter.getCapacity()) {
            throw new BusinessValidationException("Shelter capacity exceeded");
        }
        shelter.setCurrentOccupancy(newOccupancy);
        return toResponse(shelterRepository.save(shelter));
    }

    public Page<ReliefShelterResponseDto> getPaginatedShelters(Pageable pageable) {
        return shelterRepository.findAll(pageable).map(this::toResponse);
    }

    public ReliefShelterResponseDto getShelter(Long id) {
        return toResponse(findShelter(id));
    }

    @Transactional
    public ReliefShelterResponseDto updateShelter(Long id, ReliefShelterRequestDto dto) {
        ReliefShelter shelter = findShelter(id);
        applyFields(shelter, dto);
        return toResponse(shelterRepository.save(shelter));
    }

    @Transactional
    public void deleteShelter(Long id) {
        ReliefShelter shelter = findShelter(id);
        shelter.setActive(false);
        shelterRepository.save(shelter);
    }

    private void applyFields(ReliefShelter shelter, ReliefShelterRequestDto dto) {
        int occupancy = dto.currentOccupancy() == null ? 0 : dto.currentOccupancy();
        if (occupancy > dto.capacity()) {
            throw new BusinessValidationException("Current occupancy cannot exceed shelter capacity");
        }
        shelter.setShelterName(dto.shelterName());
        shelter.setLocationAddress(dto.locationAddress());
        shelter.setCapacity(dto.capacity());
        shelter.setCurrentOccupancy(occupancy);
        shelter.setManagerName(dto.managerName());
    }

    private ReliefShelter findShelter(Long id) {
        return shelterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReliefShelter not found"));
    }

    private ReliefShelterResponseDto toResponse(ReliefShelter shelter) {
        return new ReliefShelterResponseDto(
                shelter.getId(),
                shelter.getShelterName(),
                shelter.getLocationAddress(),
                shelter.getCapacity(),
                shelter.getCurrentOccupancy(),
                shelter.getManagerName(),
                shelter.isActive()
        );
    }
}
