package com.xtensus.hrmanagementapi.repository;
import com.xtensus.hrmanagementapi.domain.entity.PipelineValidation; import java.util.List; import java.util.Optional; import org.springframework.data.jpa.repository.JpaRepository;
public interface PipelineValidationRepository extends JpaRepository<PipelineValidation,Long>{
 List<PipelineValidation> findAllByOrderByNomAsc(); Optional<PipelineValidation> findFirstByEmployeIdAndActifTrue(Long employeId); boolean existsByEmployeIdAndActifTrueAndIdNot(Long employeId,Long id);
}
