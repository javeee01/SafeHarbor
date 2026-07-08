package com.example.demo.service;

import com.example.demo.dto.AuthRequestDto;
import com.example.demo.dto.AuthResponseDto;
import com.example.demo.dto.PersonnelAccountRequestDto;
import com.example.demo.dto.PersonnelAccountResponseDto;
import com.example.demo.entity.PersonnelAccount;
import com.example.demo.exception.BusinessValidationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.PersonnelAccountRepository;
import com.example.demo.security.JwtService;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private static final List<String> ROLES = List.of(
            "AGENCY_DIRECTOR",
            "EMERGENCY_DISPATCHER",
            "FIELD_RESPONDER",
            "LOGISTICS_COORDINATOR"
    );

    private final PersonnelAccountRepository personnelAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            PersonnelAccountRepository personnelAccountRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.personnelAccountRepository = personnelAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponseDto registerPersonnel(PersonnelAccountRequestDto dto) {
        if (personnelAccountRepository.findByUsername(dto.username()).isPresent()) {
            throw new BusinessValidationException("Username already exists");
        }
        validateRole(dto.role());

        PersonnelAccount account = new PersonnelAccount();
        account.setUsername(dto.username());
        account.setPasswordHash(passwordEncoder.encode(dto.password()));
        account.setFullName(dto.fullName());
        account.setRole(dto.role());
        account.setContactNumber(dto.contactNumber());
        account.setAssignedRegion(dto.assignedRegion());
        account.setActive(true);

        PersonnelAccount saved = personnelAccountRepository.save(account);
        return tokenResponse(saved);
    }

    public AuthResponseDto authenticatePersonnel(AuthRequestDto dto) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dto.username(), dto.password()));
        PersonnelAccount account = findAccount(dto.username());
        return tokenResponse(account);
    }

    public Page<PersonnelAccountResponseDto> getPaginatedAccounts(Pageable pageable) {
        return personnelAccountRepository.findAll(pageable).map(this::toResponse);
    }

    public PersonnelAccountResponseDto getAccount(Long id) {
        return toResponse(findAccount(id));
    }

    @Transactional
    public PersonnelAccountResponseDto updatePersonnel(Long id, PersonnelAccountRequestDto dto) {
        PersonnelAccount account = findAccount(id);
        validateRole(dto.role());
        account.setUsername(dto.username());
        if (dto.password() != null && !dto.password().isBlank()) {
            account.setPasswordHash(passwordEncoder.encode(dto.password()));
        }
        account.setFullName(dto.fullName());
        account.setRole(dto.role());
        account.setContactNumber(dto.contactNumber());
        account.setAssignedRegion(dto.assignedRegion());
        return toResponse(personnelAccountRepository.save(account));
    }

    @Transactional
    public void deactivatePersonnel(Long id) {
        PersonnelAccount account = findAccount(id);
        account.setActive(false);
        personnelAccountRepository.save(account);
    }

    private AuthResponseDto tokenResponse(PersonnelAccount account) {
        UserDetails details = new User(
                account.getUsername(),
                account.getPasswordHash(),
                List.of(new SimpleGrantedAuthority(account.getRole()))
        );
        String token = jwtService.generateToken(details);
        return new AuthResponseDto(token, null, account.getUsername(), account.getRole(), account.getFullName());
    }

    private PersonnelAccount findAccount(String username) {
        return personnelAccountRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private PersonnelAccount findAccount(Long id) {
        return personnelAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PersonnelAccount not found"));
    }

    private void validateRole(String role) {
        if (!ROLES.contains(role)) {
            throw new BusinessValidationException("Invalid personnel role");
        }
    }

    private PersonnelAccountResponseDto toResponse(PersonnelAccount account) {
        return new PersonnelAccountResponseDto(
                account.getId(),
                account.getUsername(),
                account.getFullName(),
                account.getRole(),
                account.getContactNumber(),
                account.getAssignedRegion(),
                account.isActive()
        );
    }
}
