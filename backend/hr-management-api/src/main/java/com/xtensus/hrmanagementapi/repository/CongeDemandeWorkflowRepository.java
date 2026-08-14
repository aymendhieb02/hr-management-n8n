package com.xtensus.hrmanagementapi.repository;
import com.xtensus.hrmanagementapi.domain.entity.CongeDemandeWorkflow; import java.util.Optional; import org.springframework.data.jpa.repository.JpaRepository;
public interface CongeDemandeWorkflowRepository extends JpaRepository<CongeDemandeWorkflow,Long>{ Optional<CongeDemandeWorkflow> findByDemandeId(Long demandeId); }
