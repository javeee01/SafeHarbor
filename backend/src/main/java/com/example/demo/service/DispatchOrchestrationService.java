package com.example.demo.service;

import com.example.demo.dto.ResourceDispatchRequestDto;
import com.example.demo.dto.ResourceDispatchResponseDto;
import com.example.demo.entity.DisasterIncident;
import com.example.demo.entity.ResourceDispatch;
import com.example.demo.entity.SupplyInventory;
import com.example.demo.exception.BusinessValidationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.DisasterIncidentRepository;
import com.example.demo.repository.ResourceDispatchRepository;
import com.example.demo.repository.SupplyInventoryRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DispatchOrchestrationService {
    private static final List<String> STATUSES = List.of("PENDING_APPROVAL", "IN_TRANSIT", "DELIVERED", "CANCELLED");

    private final ResourceDispatchRepository dispatchRepository;
    private final DisasterIncidentRepository incidentRepository;
    private final SupplyInventoryRepository inventoryRepository;

    public DispatchOrchestrationService(
            ResourceDispatchRepository dispatchRepository,
            DisasterIncidentRepository incidentRepository,
            SupplyInventoryRepository inventoryRepository
    ) {
        this.dispatchRepository = dispatchRepository;
        this.incidentRepository = incidentRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public ResourceDispatchResponseDto requestDispatch(ResourceDispatchRequestDto dto) {
        DisasterIncident incident = incidentRepository.findById(dto.targetIncidentId())
                .orElseThrow(() -> new ResourceNotFoundException("DisasterIncident not found"));
        SupplyInventory inventory = inventoryRepository.findById(dto.inventoryItemId())
                .orElseThrow(() -> new ResourceNotFoundException("SupplyInventory not found"));

        if (dto.dispatchedQuantity() > inventory.getAvailableQuantity()) {
            throw new BusinessValidationException("Dispatched quantity exceeds available stock");
        }

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - dto.dispatchedQuantity());
        inventory.setReservedQuantity(inventory.getReservedQuantity() + dto.dispatchedQuantity());
        incident.setStatus("ASSIGNED");

        ResourceDispatch dispatch = new ResourceDispatch();
        dispatch.setTargetIncident(incident);
        dispatch.setInventory(inventory);
        dispatch.setDispatchedQuantity(dto.dispatchedQuantity());
        dispatch.setDispatchStatus("IN_TRANSIT");

        inventoryRepository.save(inventory);
        incidentRepository.save(incident);
        return toResponse(dispatchRepository.save(dispatch));
    }

    @Transactional
    public ResourceDispatchResponseDto fulfillDispatch(Long id) {
        ResourceDispatch dispatch = findDispatch(id);
        if ("DELIVERED".equals(dispatch.getDispatchStatus())) {
            throw new BusinessValidationException("Dispatch already delivered");
        }
        if ("CANCELLED".equals(dispatch.getDispatchStatus())) {
            throw new BusinessValidationException("Cancelled dispatch cannot be fulfilled");
        }

        SupplyInventory inventory = dispatch.getInventory();
        inventory.setReservedQuantity(Math.max(0, inventory.getReservedQuantity() - dispatch.getDispatchedQuantity()));
        dispatch.setDispatchStatus("DELIVERED");
        inventoryRepository.save(inventory);
        return toResponse(dispatchRepository.save(dispatch));
    }

    public Page<ResourceDispatchResponseDto> getPaginatedDispatches(Pageable pageable) {
        return dispatchRepository.findAll(pageable).map(this::toResponse);
    }

    public ResourceDispatchResponseDto getDispatch(Long id) {
        return toResponse(findDispatch(id));
    }

    @Transactional
    public ResourceDispatchResponseDto updateDispatch(Long id, ResourceDispatchRequestDto dto) {
        ResourceDispatch dispatch = findDispatch(id);
        if (!"IN_TRANSIT".equals(dispatch.getDispatchStatus()) && !"PENDING_APPROVAL".equals(dispatch.getDispatchStatus())) {
            throw new BusinessValidationException("Only active dispatches can be updated");
        }

        SupplyInventory currentInventory = dispatch.getInventory();
        currentInventory.setAvailableQuantity(currentInventory.getAvailableQuantity() + dispatch.getDispatchedQuantity());
        currentInventory.setReservedQuantity(Math.max(0, currentInventory.getReservedQuantity() - dispatch.getDispatchedQuantity()));

        DisasterIncident incident = incidentRepository.findById(dto.targetIncidentId())
                .orElseThrow(() -> new ResourceNotFoundException("DisasterIncident not found"));
        SupplyInventory inventory = inventoryRepository.findById(dto.inventoryItemId())
                .orElseThrow(() -> new ResourceNotFoundException("SupplyInventory not found"));
        if (dto.dispatchedQuantity() > inventory.getAvailableQuantity()) {
            throw new BusinessValidationException("Dispatched quantity exceeds available stock");
        }

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - dto.dispatchedQuantity());
        inventory.setReservedQuantity(inventory.getReservedQuantity() + dto.dispatchedQuantity());
        dispatch.setTargetIncident(incident);
        dispatch.setInventory(inventory);
        dispatch.setDispatchedQuantity(dto.dispatchedQuantity());
        return toResponse(dispatchRepository.save(dispatch));
    }

    @Transactional
    public void cancelDispatch(Long id) {
        ResourceDispatch dispatch = findDispatch(id);
        if ("DELIVERED".equals(dispatch.getDispatchStatus())) {
            throw new BusinessValidationException("Delivered dispatch cannot be cancelled");
        }
        SupplyInventory inventory = dispatch.getInventory();
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + dispatch.getDispatchedQuantity());
        inventory.setReservedQuantity(Math.max(0, inventory.getReservedQuantity() - dispatch.getDispatchedQuantity()));
        dispatch.setDispatchStatus("CANCELLED");
        inventoryRepository.save(inventory);
        dispatchRepository.save(dispatch);
    }

    @Transactional
    public ResourceDispatchResponseDto updateDispatchStatus(Long id, String status) {
        if (!STATUSES.contains(status)) {
            throw new BusinessValidationException("Invalid dispatch status");
        }
        ResourceDispatch dispatch = findDispatch(id);
        dispatch.setDispatchStatus(status);
        return toResponse(dispatchRepository.save(dispatch));
    }

    private ResourceDispatch findDispatch(Long id) {
        return dispatchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ResourceDispatch not found"));
    }

    private ResourceDispatchResponseDto toResponse(ResourceDispatch dispatch) {
        return new ResourceDispatchResponseDto(
                dispatch.getId(),
                dispatch.getTargetIncident().getTitle(),
                dispatch.getInventory().getItemName(),
                dispatch.getDispatchedQuantity(),
                dispatch.getDispatchStatus(),
                dispatch.getInitiatedAt()
        );
    }
}
