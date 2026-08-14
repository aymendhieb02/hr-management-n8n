package com.xtensus.hrmanagementapi.repository;
import com.xtensus.hrmanagementapi.domain.entity.EmailOutbox; import java.time.LocalDateTime; import java.util.List; import org.springframework.data.jpa.repository.JpaRepository;
public interface EmailOutboxRepository extends JpaRepository<EmailOutbox,Long>{ List<EmailOutbox> findTop20ByStatutAndProchaineTentativeLessThanEqualOrderByDateCreationAsc(String statut,LocalDateTime now); }
