package com.xtensus.hrmanagementapi.security.user;

import com.xtensus.hrmanagementapi.repository.EmployeRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final EmployeRepository employeRepository;
    public CustomUserDetailsService(EmployeRepository employeRepository) { this.employeRepository = employeRepository; }
    @Override public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return employeRepository.findByUsernameIgnoreCase(username).map(CustomUserDetails::new).orElseThrow(() -> new UsernameNotFoundException("Employe introuvable"));
    }
}
