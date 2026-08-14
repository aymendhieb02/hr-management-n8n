package com.xtensus.hrmanagementapi.repository;

import com.xtensus.hrmanagementapi.domain.entity.VariableSysteme;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VariableSystemeRepository extends JpaRepository<VariableSysteme, Long> {
    Optional<VariableSysteme> findByNom(String nom);
}
