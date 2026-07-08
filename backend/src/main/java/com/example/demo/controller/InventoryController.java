package com.example.demo.controller;

import com.example.demo.dto.MessageResponseDto;
import com.example.demo.dto.SupplyInventoryRequestDto;
import com.example.demo.dto.SupplyInventoryResponseDto;
import com.example.demo.service.InventoryLogisticsService;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/inventory")
public class InventoryController {
    private final InventoryLogisticsService inventoryService;

    public InventoryController(InventoryLogisticsService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('AGENCY_DIRECTOR', 'EMERGENCY_DISPATCHER')")
    public ResponseEntity<SupplyInventoryResponseDto> addInventory(@Valid @RequestBody SupplyInventoryRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.addNewSupplyItem(dto));
    }

    @GetMapping
    public ResponseEntity<Page<SupplyInventoryResponseDto>> getInventory(Pageable pageable) {
        return ResponseEntity.ok(inventoryService.getPaginatedInventory(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplyInventoryResponseDto> getInventoryItem(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getInventory(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('AGENCY_DIRECTOR', 'EMERGENCY_DISPATCHER')")
    public ResponseEntity<SupplyInventoryResponseDto> updateInventory(
            @PathVariable Long id,
            @Valid @RequestBody SupplyInventoryRequestDto dto
    ) {
        return ResponseEntity.ok(inventoryService.updateInventoryItem(id, dto));
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyAuthority('AGENCY_DIRECTOR', 'EMERGENCY_DISPATCHER')")
    public ResponseEntity<Void> updateInventoryStock(@PathVariable Long id, @RequestParam Integer quantity) {
        inventoryService.updateInventory(id, quantity);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/shortages")
    public ResponseEntity<List<SupplyInventoryResponseDto>> shortages() {
        return ResponseEntity.ok(inventoryService.getCriticalShortages());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('AGENCY_DIRECTOR', 'EMERGENCY_DISPATCHER')")
    public ResponseEntity<MessageResponseDto> deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.ok(new MessageResponseDto("SupplyInventory deleted successfully."));
    }
}
