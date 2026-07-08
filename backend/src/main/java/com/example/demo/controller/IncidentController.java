package com.example.demo.controller;

import com.example.demo.dto.DisasterIncidentRequestDto;
import com.example.demo.dto.DisasterIncidentResponseDto;
import com.example.demo.dto.MessageResponseDto;
import com.example.demo.service.IncidentCoordinationService;
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
@RequestMapping("/api/incidents")
public class IncidentController {
    private final IncidentCoordinationService incidentService;

    public IncidentController(IncidentCoordinationService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('AGENCY_DIRECTOR', 'EMERGENCY_DISPATCHER')")
    public ResponseEntity<DisasterIncidentResponseDto> reportIncident(@Valid @RequestBody DisasterIncidentRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incidentService.reportNewIncident(dto));
    }

    @GetMapping
    public ResponseEntity<Page<DisasterIncidentResponseDto>> getIncidents(Pageable pageable) {
        return ResponseEntity.ok(incidentService.getPaginatedIncidents(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DisasterIncidentResponseDto> getIncident(@PathVariable Long id) {
        return ResponseEntity.ok(incidentService.getIncident(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('AGENCY_DIRECTOR', 'EMERGENCY_DISPATCHER')")
    public ResponseEntity<DisasterIncidentResponseDto> updateIncident(
            @PathVariable Long id,
            @Valid @RequestBody DisasterIncidentRequestDto dto
    ) {
        return ResponseEntity.ok(incidentService.updateIncident(id, dto));
    }

    @PatchMapping("/{id}/assign/{personnelId}")
    @PreAuthorize("hasAnyAuthority('AGENCY_DIRECTOR', 'EMERGENCY_DISPATCHER')")
    public ResponseEntity<DisasterIncidentResponseDto> assignResponder(
            @PathVariable Long id,
            @PathVariable Long personnelId
    ) {
        return ResponseEntity.ok(incidentService.assignResponder(id, personnelId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        incidentService.updateIncidentStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('AGENCY_DIRECTOR', 'EMERGENCY_DISPATCHER')")
    public ResponseEntity<MessageResponseDto> deleteIncident(@PathVariable Long id) {
        incidentService.deleteIncident(id);
        return ResponseEntity.ok(new MessageResponseDto("DisasterIncident deleted successfully."));
    }
}
