package com.example.demo.controller;

import com.example.demo.dto.MessageResponseDto;
import com.example.demo.dto.ReliefShelterRequestDto;
import com.example.demo.dto.ReliefShelterResponseDto;
import com.example.demo.service.ShelterManagementService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shelters")
public class ShelterController {
    private final ShelterManagementService shelterService;

    public ShelterController(ShelterManagementService shelterService) {
        this.shelterService = shelterService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('AGENCY_DIRECTOR', 'EMERGENCY_DISPATCHER')")
    public ResponseEntity<ReliefShelterResponseDto> registerShelter(@Valid @RequestBody ReliefShelterRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shelterService.registerShelter(dto));
    }

    @GetMapping
    public ResponseEntity<Page<ReliefShelterResponseDto>> getShelters(Pageable pageable) {
        return ResponseEntity.ok(shelterService.getPaginatedShelters(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReliefShelterResponseDto> getShelter(@PathVariable Long id) {
        return ResponseEntity.ok(shelterService.getShelter(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('AGENCY_DIRECTOR', 'EMERGENCY_DISPATCHER')")
    public ResponseEntity<ReliefShelterResponseDto> updateShelter(
            @PathVariable Long id,
            @Valid @RequestBody ReliefShelterRequestDto dto
    ) {
        return ResponseEntity.ok(shelterService.updateShelter(id, dto));
    }

    @PatchMapping("/{id}/occupancy")
    @PreAuthorize("hasAnyAuthority('AGENCY_DIRECTOR', 'EMERGENCY_DISPATCHER')")
    public ResponseEntity<ReliefShelterResponseDto> adjustOccupancy(
            @PathVariable Long id,
            @RequestParam int intakeCount
    ) {
        return ResponseEntity.ok(shelterService.adjustOccupancy(id, intakeCount));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('AGENCY_DIRECTOR', 'EMERGENCY_DISPATCHER')")
    public ResponseEntity<MessageResponseDto> deleteShelter(@PathVariable Long id) {
        shelterService.deleteShelter(id);
        return ResponseEntity.ok(new MessageResponseDto("ReliefShelter deleted successfully."));
    }
}
