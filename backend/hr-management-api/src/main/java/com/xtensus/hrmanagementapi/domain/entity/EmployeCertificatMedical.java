package com.xtensus.hrmanagementapi.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
@Entity @Table(name="employe_certificat_medicals") @Getter @Setter @NoArgsConstructor
public class EmployeCertificatMedical {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="employe_certificat_medical_id") private Long id;
 @OneToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="conge_demande_id", nullable=false, unique=true) private CongeDemande congeDemande;
 @Column(name="employe_certificat_medical_nom_fichier", nullable=false, length=255) private String nomFichier;
 @Column(name="employe_certificat_medical_lien_fichier", nullable=false, length=500) private String lienFichier;
 @Column(name="employe_certificat_medical_type_mime", nullable=false, length=100) private String typeMime;
 @Column(name="employe_certificat_medical_taille_fichier", nullable=false) private Long tailleFichier;
 @Column(name="employe_certificat_medical_date_soumission", nullable=false) private LocalDateTime dateSoumission;
}
