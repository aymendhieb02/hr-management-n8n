package com.xtensus.hrmanagementapi.repository;
import com.xtensus.hrmanagementapi.domain.entity.CongeDemandeWorkflowEtape; import java.util.List; import java.util.Optional; import org.springframework.data.jpa.repository.JpaRepository;
public interface CongeDemandeWorkflowEtapeRepository extends JpaRepository<CongeDemandeWorkflowEtape,Long>{
 Optional<CongeDemandeWorkflowEtape> findByWorkflowIdAndPriorite(Long workflowId,Integer priorite);
 List<CongeDemandeWorkflowEtape> findByDecideurIdAndStatutOrderByWorkflowDemandeDateSoumissionDesc(Long decideurId,String statut);
 List<CongeDemandeWorkflowEtape> findByStatutOrderByWorkflowDemandeDateSoumissionDesc(String statut);
}
