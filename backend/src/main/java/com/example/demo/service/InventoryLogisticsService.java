package com.example.demo.service;

import com.example.demo.dto.SupplyInventoryRequestDto;
import com.example.demo.dto.SupplyInventoryResponseDto;
import com.example.demo.entity.SupplyInventory;
import com.example.demo.exception.BusinessValidationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.SupplyInventoryRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryLogisticsService {
    private final SupplyInventoryRepository inventoryRepository;

    public InventoryLogisticsService(SupplyInventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public SupplyInventoryResponseDto addNewSupplyItem(SupplyInventoryRequestDto dto) {
        if (inventoryRepository.findByItemName(dto.itemName()).isPresent()) {
            throw new BusinessValidationException("Inventory item already exists");
        }
        SupplyInventory inventory = new SupplyInventory();
        applyFields(inventory, dto);
        inventory.setReservedQuantity(0);
        return toResponse(inventoryRepository.save(inventory));
    }

    @Transactional
    public void updateInventory(Long id, Integer quantity) {
        if (quantity == null || quantity < 0) {
            throw new BusinessValidationException("Quantity must be zero or greater");
        }
        SupplyInventory inventory = findInventory(id);
        inventory.setAvailableQuantity(quantity);
        inventoryRepository.save(inventory);
    }

    public List<SupplyInventoryResponseDto> getCriticalShortages() {
        return inventoryRepository.findItemsBelowCriticalThreshold().stream().map(this::toResponse).toList();
    }

    public Page<SupplyInventoryResponseDto> getPaginatedInventory(Pageable pageable) {
        return inventoryRepository.findAll(pageable).map(this::toResponse);
    }

    public SupplyInventoryResponseDto getInventory(Long id) {
        return toResponse(findInventory(id));
    }

    @Transactional
    public SupplyInventoryResponseDto updateInventoryItem(Long id, SupplyInventoryRequestDto dto) {
        SupplyInventory inventory = findInventory(id);
        applyFields(inventory, dto);
        return toResponse(inventoryRepository.save(inventory));
    }

    @Transactional
    public void deleteInventory(Long id) {
        inventoryRepository.delete(findInventory(id));
    }

    private void applyFields(SupplyInventory inventory, SupplyInventoryRequestDto dto) {
        if (dto.criticalThreshold() != null && dto.criticalThreshold() < 1) {
            throw new BusinessValidationException("Critical threshold must be a positive integer");
        }
        inventory.setItemName(dto.itemName());
        inventory.setCategory(dto.category());
        inventory.setAvailableQuantity(dto.availableQuantity() == null ? 0 : dto.availableQuantity());
        inventory.setCriticalThreshold(dto.criticalThreshold() == null ? 100 : dto.criticalThreshold());
        inventory.setUnit(dto.unit());
    }

    private SupplyInventory findInventory(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupplyInventory not found"));
    }

    private SupplyInventoryResponseDto toResponse(SupplyInventory inventory) {
        return new SupplyInventoryResponseDto(
                inventory.getId(),
                inventory.getItemName(),
                inventory.getCategory(),
                inventory.getAvailableQuantity(),
                inventory.getReservedQuantity(),
                inventory.getCriticalThreshold(),
                inventory.getUnit()
        );
    }
}
