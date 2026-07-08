package com.example.demo.controller;

import com.example.demo.dto.AuthRequestDto;
import com.example.demo.dto.AuthResponseDto;
import com.example.demo.dto.PersonnelAccountRequestDto;
import com.example.demo.dto.PersonnelAccountResponseDto;
import com.example.demo.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody PersonnelAccountRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerPersonnel(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody AuthRequestDto dto) {
        return ResponseEntity.ok(authService.authenticatePersonnel(dto));
    }

    @GetMapping("/personnel")
    @PreAuthorize("hasAuthority('AGENCY_DIRECTOR')")
    public ResponseEntity<Page<PersonnelAccountResponseDto>> personnel(Pageable pageable) {
        return ResponseEntity.ok(authService.getPaginatedAccounts(pageable));
    }

    @GetMapping("/personnel/{id}")
    @PreAuthorize("hasAuthority('AGENCY_DIRECTOR')")
    public ResponseEntity<PersonnelAccountResponseDto> getPersonnel(@PathVariable Long id) {
        return ResponseEntity.ok(authService.getAccount(id));
    }

    @PutMapping("/personnel/{id}")
    @PreAuthorize("hasAuthority('AGENCY_DIRECTOR')")
    public ResponseEntity<PersonnelAccountResponseDto> updatePersonnel(
            @PathVariable Long id,
            @Valid @RequestBody PersonnelAccountRequestDto dto
    ) {
        return ResponseEntity.ok(authService.updatePersonnel(id, dto));
    }

    @DeleteMapping("/personnel/{id}")
    @PreAuthorize("hasAuthority('AGENCY_DIRECTOR')")
    public ResponseEntity<Void> deactivatePersonnel(@PathVariable Long id) {
        authService.deactivatePersonnel(id);
        return ResponseEntity.noContent().build();
    }
}
