package com.example.demo.controller;

import com.example.demo.dto.MessageResponseDto;
import com.example.demo.dto.ResourceDispatchRequestDto;
import com.example.demo.dto.ResourceDispatchResponseDto;
import com.example.demo.service.DispatchOrchestrationService;
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
@RequestMapping("/api/dispatches")
public class DispatchController {
    private final DispatchOrchestrationService dispatchService;

    public DispatchController(DispatchOrchestrationService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @PostMapping("/request")
    @PreAuthorize("hasAnyAuthority('AGENCY_DIRECTOR', 'EMERGENCY_DISPATCHER')")
    public ResponseEntity<ResourceDispatchResponseDto> requestDispatch(@Valid @RequestBody ResourceDispatchRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dispatchService.requestDispatch(dto));
    }

    @PostMapping("/{id}/fulfill")
    @PreAuthorize("hasAnyAuthority('AGENCY_DIRECTOR', 'LOGISTICS_COORDINATOR')")
    public ResponseEntity<ResourceDispatchResponseDto> fulfillDispatch(@PathVariable Long id) {
        return ResponseEntity.ok(dispatchService.fulfillDispatch(id));
    }

    @GetMapping
    public ResponseEntity<Page<ResourceDispatchResponseDto>> getDispatches(Pageable pageable) {
        return ResponseEntity.ok(dispatchService.getPaginatedDispatches(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceDispatchResponseDto> getDispatch(@PathVariable Long id) {
        return ResponseEntity.ok(dispatchService.getDispatch(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('AGENCY_DIRECTOR', 'EMERGENCY_DISPATCHER')")
    public ResponseEntity<ResourceDispatchResponseDto> updateDispatch(
            @PathVariable Long id,
            @Valid @RequestBody ResourceDispatchRequestDto dto
    ) {
        return ResponseEntity.ok(dispatchService.updateDispatch(id, dto));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('AGENCY_DIRECTOR', 'EMERGENCY_DISPATCHER', 'LOGISTICS_COORDINATOR')")
    public ResponseEntity<ResourceDispatchResponseDto> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(dispatchService.updateDispatchStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('AGENCY_DIRECTOR', 'EMERGENCY_DISPATCHER')")
    public ResponseEntity<MessageResponseDto> cancelDispatch(@PathVariable Long id) {
        dispatchService.cancelDispatch(id);
        return ResponseEntity.ok(new MessageResponseDto("ResourceDispatch cancelled successfully."));
    }
}
