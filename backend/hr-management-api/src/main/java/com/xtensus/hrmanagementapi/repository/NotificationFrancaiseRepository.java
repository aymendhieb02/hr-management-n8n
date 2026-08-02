package com.xtensus.hrmanagementapi.repository;

import com.xtensus.hrmanagementapi.domain.entity.NotificationFrancaise; import java.util.List; import org.springframework.data.jpa.repository.JpaRepository;
public interface NotificationFrancaiseRepository extends JpaRepository<NotificationFrancaise, Long> {
 List<NotificationFrancaise> findByEmployeIdOrderByDateCreationDesc(Long employeId);
 List<NotificationFrancaise> findByEmployeIdAndLuFalseOrderByDateCreationDesc(Long employeId);
}
