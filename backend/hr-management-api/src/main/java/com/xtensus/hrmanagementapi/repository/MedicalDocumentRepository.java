package com.xtensus.hrmanagementapi.repository;

import com.xtensus.hrmanagementapi.domain.entity.MedicalDocument;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicalDocumentRepository extends JpaRepository<MedicalDocument, Long> {

    Optional<MedicalDocument> findByLeaveRequestId(Long leaveRequestId);
}
