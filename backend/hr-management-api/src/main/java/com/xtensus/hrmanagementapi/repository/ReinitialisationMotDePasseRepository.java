package com.xtensus.hrmanagementapi.repository;

import com.xtensus.hrmanagementapi.domain.entity.ReinitialisationMotDePasse;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReinitialisationMotDePasseRepository extends JpaRepository<ReinitialisationMotDePasse, Long> {
    Optional<ReinitialisationMotDePasse> findFirstByEmployeIdAndUtiliseFalseOrderByDateCreationDesc(Long employeId);
}
