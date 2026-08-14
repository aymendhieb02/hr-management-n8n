package com.xtensus.hrmanagementapi.workflow;
import com.xtensus.hrmanagementapi.domain.entity.*; import com.xtensus.hrmanagementapi.domain.enums.RoleType; import com.xtensus.hrmanagementapi.repository.*; import java.time.LocalDateTime; import java.util.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service public class PipelineValidationService{
 private final PipelineValidationRepository pipelines; private final EmployeRepository employes;
 public PipelineValidationService(PipelineValidationRepository p,EmployeRepository e){pipelines=p;employes=e;}
 public record EtapeRequest(Long decideurId,Integer priorite){} public record Request(String nom,Long employeId,Boolean actif,List<EtapeRequest> etapes){}
 public record Personne(Long id,String nom,String prenom,String role){} public record Etape(Long id,Integer priorite,Personne decideur,Boolean actif){}
 public record Response(Long id,String nom,Personne employe,Boolean actif,List<Etape> etapes){}
 @Transactional(readOnly=true) public List<Response> lister(){return pipelines.findAllByOrderByNomAsc().stream().map(this::dto).toList();}
 @Transactional public Response creer(Request r){return enregistrer(new PipelineValidation(),r);}
 @Transactional public Response modifier(Long id,Request r){return enregistrer(pipelines.findById(id).orElseThrow(()->new IllegalArgumentException("Circuit de validation introuvable")),r);}
 @Transactional public void supprimer(Long id){PipelineValidation p=pipelines.findById(id).orElseThrow(()->new IllegalArgumentException("Circuit introuvable")); p.setActif(false);p.setDateModification(LocalDateTime.now());pipelines.save(p);}
 private Response enregistrer(PipelineValidation p,Request r){
  if(r.nom()==null||r.nom().isBlank())throw new IllegalArgumentException("Le nom du circuit est obligatoire"); if(r.etapes()==null||r.etapes().isEmpty())throw new IllegalArgumentException("Le circuit doit contenir au moins une étape");
  Employe cible=employes.findById(r.employeId()).orElseThrow(()->new IllegalArgumentException("Employé introuvable")); boolean actif=!Boolean.FALSE.equals(r.actif());
  if(actif&&pipelines.existsByEmployeIdAndActifTrueAndIdNot(cible.getId(),p.getId()==null?-1L:p.getId()))throw new IllegalArgumentException("Cet employé possède déjà un circuit actif");
  Set<Integer> priorites=new HashSet<>();Set<Long> decideurs=new HashSet<>();List<PipelineValidationEtape> nouvelles=new ArrayList<>();
  for(EtapeRequest e:r.etapes()){
   if(e.priorite()==null||e.priorite()<1||!priorites.add(e.priorite()))throw new IllegalArgumentException("Les priorités doivent être uniques et positives");
   if(e.decideurId()==null||!decideurs.add(e.decideurId()))throw new IllegalArgumentException("Un décideur ne peut apparaître qu'une fois");
   Employe d=employes.findById(e.decideurId()).orElseThrow(()->new IllegalArgumentException("Décideur introuvable")); RoleType role=RoleType.fromDatabaseRole(d.getRole());
   if(!Boolean.TRUE.equals(d.getActif())||!(role==RoleType.DG||role==RoleType.DT||role==RoleType.HR||role==RoleType.ADMIN))throw new IllegalArgumentException("Le décideur doit être DG, DT, RH ou Administrateur actif");
   PipelineValidationEtape pe=new PipelineValidationEtape();pe.setPipeline(p);pe.setDecideur(d);pe.setPriorite(e.priorite());pe.setActif(true);nouvelles.add(pe);
  }
  p.setNom(r.nom().trim());p.setEmploye(cible);p.setActif(actif);if(p.getId()==null)p.setDateCreation(LocalDateTime.now());else p.setDateModification(LocalDateTime.now());p.getEtapes().clear();p.getEtapes().addAll(nouvelles);return dto(pipelines.save(p));
 }
 private Response dto(PipelineValidation p){return new Response(p.getId(),p.getNom(),personne(p.getEmploye()),p.getActif(),p.getEtapes().stream().map(e->new Etape(e.getId(),e.getPriorite(),personne(e.getDecideur()),e.getActif())).toList());}
 private Personne personne(Employe e){return new Personne(e.getId(),e.getNom(),e.getPrenom(),RoleType.fromDatabaseRole(e.getRole()).name());}
}
