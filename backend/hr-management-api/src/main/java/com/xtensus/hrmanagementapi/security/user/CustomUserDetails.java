package com.xtensus.hrmanagementapi.security.user;

import com.xtensus.hrmanagementapi.domain.entity.Employe;
import com.xtensus.hrmanagementapi.domain.enums.RoleType;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserDetails implements UserDetails {
    private final Long id;
    private final String username;
    private final String passwordHash;
    private final RoleType role;
    private final Boolean actif;
    private final String statut;

    public CustomUserDetails(Employe employe) {
        this.id = employe.getId();
        this.username = employe.getUsername();
        this.passwordHash = employe.getMotDePasseHash();
        this.role = RoleType.fromDatabaseRole(employe.getRole());
        this.actif = employe.getActif();
        this.statut = employe.getStatut();
    }

    public Long getId() { return id; }
    public RoleType getRole() { return role; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return List.of(new SimpleGrantedAuthority("ROLE_" + role.name())); }
    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return Boolean.TRUE.equals(actif) && "ACTIF".equalsIgnoreCase(statut); }
}
