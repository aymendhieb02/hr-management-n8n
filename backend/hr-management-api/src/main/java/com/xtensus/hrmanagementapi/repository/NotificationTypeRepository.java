package com.xtensus.hrmanagementapi.repository;

import com.xtensus.hrmanagementapi.domain.entity.NotificationType; import java.util.List; import java.util.Optional; import org.springframework.data.jpa.repository.JpaRepository;
public interface NotificationTypeRepository extends JpaRepository<NotificationType, Long> {
 Optional<NotificationType> findByLibelle(String libelle); List<NotificationType> findByActifTrue();
}
