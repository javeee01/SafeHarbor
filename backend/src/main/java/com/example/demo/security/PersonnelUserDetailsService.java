package com.example.demo.security;

import com.example.demo.entity.PersonnelAccount;
import com.example.demo.repository.PersonnelAccountRepository;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PersonnelUserDetailsService implements UserDetailsService {
    private final PersonnelAccountRepository personnelAccountRepository;

    public PersonnelUserDetailsService(PersonnelAccountRepository personnelAccountRepository) {
        this.personnelAccountRepository = personnelAccountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        PersonnelAccount account = personnelAccountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new User(
                account.getUsername(),
                account.getPasswordHash(),
                account.isActive(),
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority(account.getRole()))
        );
    }
}
