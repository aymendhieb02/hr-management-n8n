package com.xtensus.hrmanagementapi.domain.entity;
import jakarta.persistence.*; import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
@Entity @Table(name="pipeline_validation_etapes") @Getter @Setter @NoArgsConstructor
public class PipelineValidationEtape {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="pipeline_validation_etape_id") private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="pipeline_validation_id") private PipelineValidation pipeline;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="decideur_id") private Employe decideur;
 @Column(nullable=false) private Integer priorite; @Column(nullable=false) private Boolean actif;
}
