package com.xtensus.hrmanagementapi.domain.entity;
import jakarta.persistence.*; import java.time.LocalDateTime; import java.util.ArrayList; import java.util.List; import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
@Entity @Table(name="pipelines_validation") @Getter @Setter @NoArgsConstructor
public class PipelineValidation {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="pipeline_validation_id") private Long id;
 @Column(name="pipeline_validation_nom",nullable=false,length=150) private String nom;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="employe_id") private Employe employe;
 @Column(name="pipeline_validation_actif",nullable=false) private Boolean actif;
 @Column(name="date_creation",nullable=false) private LocalDateTime dateCreation;
 @Column(name="date_modification") private LocalDateTime dateModification;
 @OneToMany(mappedBy="pipeline",cascade=CascadeType.ALL,orphanRemoval=true) @OrderBy("priorite asc") private List<PipelineValidationEtape> etapes=new ArrayList<>();
}
