package com.example.demo.service;

import com.example.demo.dto.DisasterIncidentRequestDto;
import com.example.demo.dto.DisasterIncidentResponseDto;
import com.example.demo.entity.DisasterIncident;
import com.example.demo.entity.PersonnelAccount;
import com.example.demo.exception.BusinessValidationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.DisasterIncidentRepository;
import com.example.demo.repository.PersonnelAccountRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IncidentCoordinationService {
    private static final List<String> STATUSES = List.of("REPORTED", "ASSIGNED", "RESOLVED", "CANCELLED");

    private final DisasterIncidentRepository incidentRepository;
    private final PersonnelAccountRepository personnelAccountRepository;

    public IncidentCoordinationService(
            DisasterIncidentRepository incidentRepository,
            PersonnelAccountRepository personnelAccountRepository
    ) {
        this.incidentRepository = incidentRepository;
        this.personnelAccountRepository = personnelAccountRepository;
    }

    @Transactional
    public DisasterIncidentResponseDto reportNewIncident(DisasterIncidentRequestDto dto) {
        validateCoordinates(dto.latitude(), dto.longitude());
        DisasterIncident incident = new DisasterIncident();
        applyIncidentFields(incident, dto);
        incident.setStatus("REPORTED");
        return toResponse(incidentRepository.save(incident));
    }

    @Transactional
    public DisasterIncidentResponseDto assignResponder(Long incidentId, Long personnelId) {
        DisasterIncident incident = findIncident(incidentId);
        PersonnelAccount responder = personnelAccountRepository.findById(personnelId)
                .orElseThrow(() -> new ResourceNotFoundException("PersonnelAccount not found"));
        incident.setAssignedResponder(responder);
        incident.setStatus("ASSIGNED");
        return toResponse(incidentRepository.save(incident));
    }

    @Transactional
    public void updateIncidentStatus(Long id, String status) {
        validateStatus(status);
        DisasterIncident incident = findIncident(id);
        if ("CANCELLED".equals(incident.getStatus()) || "RESOLVED".equals(incident.getStatus())) {
            throw new BusinessValidationException("Finalized incidents cannot transition status");
        }
        incident.setStatus(status);
        incidentRepository.save(incident);
    }

    public Page<DisasterIncidentResponseDto> getPaginatedIncidents(Pageable pageable) {
        return incidentRepository.findAll(pageable).map(this::toResponse);
    }

    public DisasterIncidentResponseDto getIncident(Long id) {
        return toResponse(findIncident(id));
    }

    @Transactional
    public DisasterIncidentResponseDto updateIncident(Long id, DisasterIncidentRequestDto dto) {
        validateCoordinates(dto.latitude(), dto.longitude());
        DisasterIncident incident = findIncident(id);
        applyIncidentFields(incident, dto);
        return toResponse(incidentRepository.save(incident));
    }

    @Transactional
    public void deleteIncident(Long id) {
        DisasterIncident incident = findIncident(id);
        incidentRepository.delete(incident);
    }

    private void applyIncidentFields(DisasterIncident incident, DisasterIncidentRequestDto dto) {
        incident.setTitle(dto.title());
        incident.setDescription(dto.description());
        incident.setIncidentType(dto.incidentType());
        incident.setSeverityLevel(dto.severityLevel());
        incident.setLatitude(dto.latitude());
        incident.setLongitude(dto.longitude());
    }

    private DisasterIncident findIncident(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DisasterIncident not found"));
    }

    private void validateCoordinates(Double latitude, Double longitude) {
        if (latitude == null || latitude < -90 || latitude > 90) {
            throw new BusinessValidationException("Latitude must be between -90 and 90");
        }
        if (longitude == null || longitude < -180 || longitude > 180) {
            throw new BusinessValidationException("Longitude must be between -180 and 180");
        }
    }

    private void validateStatus(String status) {
        if (!STATUSES.contains(status)) {
            throw new BusinessValidationException("Invalid incident status");
        }
    }

    private DisasterIncidentResponseDto toResponse(DisasterIncident incident) {
        String assignedUsername = incident.getAssignedResponder() == null
                ? null
                : incident.getAssignedResponder().getUsername();
        return new DisasterIncidentResponseDto(
                incident.getId(),
                incident.getTitle(),
                incident.getDescription(),
                incident.getIncidentType(),
                incident.getSeverityLevel(),
                incident.getLatitude(),
                incident.getLongitude(),
                incident.getStatus(),
                incident.getReportedAt(),
                assignedUsername
        );
    }
}
