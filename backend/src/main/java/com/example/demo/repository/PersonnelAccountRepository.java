package com.example.demo.repository;

import com.example.demo.entity.PersonnelAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonnelAccountRepository extends JpaRepository<PersonnelAccount, Long> {
    Optional<PersonnelAccount> findByUsername(String username);
    List<PersonnelAccount> findByRoleAndIsActiveTrue(String role);
}
