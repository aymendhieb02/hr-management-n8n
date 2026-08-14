package com.xtensus.hrmanagementapi.domain.entity;
import jakarta.persistence.*; import java.time.LocalDateTime; import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
@Entity @Table(name="email_outbox") @Getter @Setter @NoArgsConstructor
public class EmailOutbox {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="email_outbox_id") private Long id;
 @Column(nullable=false,length=150) private String destinataire; @Column(nullable=false,length=250) private String sujet;
 @Column(name="contenu_html",nullable=false,columnDefinition="LONGTEXT") private String contenuHtml;
 @Column(nullable=false,length=30) private String statut; @Column(name="nombre_tentatives",nullable=false) private Integer nombreTentatives;
 @Column(name="prochaine_tentative",nullable=false) private LocalDateTime prochaineTentative; @Column(name="derniere_erreur",length=1000) private String derniereErreur;
 @Column(name="date_creation",nullable=false) private LocalDateTime dateCreation; @Column(name="date_envoi") private LocalDateTime dateEnvoi;
}
