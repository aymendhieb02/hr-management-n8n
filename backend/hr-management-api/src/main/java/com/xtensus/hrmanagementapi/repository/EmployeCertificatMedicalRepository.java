package com.xtensus.hrmanagementapi.repository;

import com.xtensus.hrmanagementapi.domain.entity.EmployeCertificatMedical;
import java.util.List; import java.util.Optional; import org.springframework.data.jpa.repository.JpaRepository;
public interface EmployeCertificatMedicalRepository extends JpaRepository<EmployeCertificatMedical, Long> {
 Optional<EmployeCertificatMedical> findByCongeDemandeId(Long congeDemandeId);
 boolean existsByCongeDemandeId(Long congeDemandeId);
 List<EmployeCertificatMedical> findByCongeDemandeEmployeId(Long employeId);
}
