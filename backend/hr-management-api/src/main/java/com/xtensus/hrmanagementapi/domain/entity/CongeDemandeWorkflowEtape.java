package com.xtensus.hrmanagementapi.domain.entity;
import jakarta.persistence.*; import java.time.LocalDateTime; import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
@Entity @Table(name="conge_demande_workflow_etapes") @Getter @Setter @NoArgsConstructor
public class CongeDemandeWorkflowEtape {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="conge_demande_workflow_etape_id") private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="conge_demande_workflow_id") private CongeDemandeWorkflow workflow;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="decideur_id") private Employe decideur;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="decideur_initial_id") private Employe decideurInitial;
 @Column(nullable=false) private Integer priorite; @Column(nullable=false,length=30) private String statut;
 @Column(length=1000) private String commentaire; @Column(name="date_action") private LocalDateTime dateAction;
 @Column(name="date_reaffectation") private LocalDateTime dateReaffectation;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="reaffecte_par_id") private Employe reaffectePar;
}
