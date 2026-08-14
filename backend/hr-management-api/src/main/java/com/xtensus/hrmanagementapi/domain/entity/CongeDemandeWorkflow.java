package com.xtensus.hrmanagementapi.domain.entity;
import jakarta.persistence.*; import java.time.LocalDateTime; import java.util.ArrayList; import java.util.List; import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
@Entity @Table(name="conge_demande_workflows") @Getter @Setter @NoArgsConstructor
public class CongeDemandeWorkflow {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="conge_demande_workflow_id") private Long id;
 @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="conge_demande_id") private CongeDemande demande;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="pipeline_source_id") private PipelineValidation pipelineSource;
 @Column(nullable=false,length=30) private String statut; @Column(name="etape_courante") private Integer etapeCourante;
 @Column(name="date_creation",nullable=false) private LocalDateTime dateCreation; @Column(name="date_fin") private LocalDateTime dateFin;
 @OneToMany(mappedBy="workflow",cascade=CascadeType.ALL,orphanRemoval=true) @OrderBy("priorite asc") private List<CongeDemandeWorkflowEtape> etapes=new ArrayList<>();
}
