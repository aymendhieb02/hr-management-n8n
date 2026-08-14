package com.xtensus.hrmanagementapi.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Table(name = "employes")
@DynamicInsert
@Getter
@Setter
@NoArgsConstructor
public class Employe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employe_id")
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "mot_de_passe_hash", nullable = false, length = 255)
    private String motDePasseHash;

    @Column(name = "code_activation_hash", length = 255)
    private String codeActivationHash;

    @Column(name = "changement_mot_de_passe_requis", nullable = false)
    private Boolean changementMotDePasseRequis;

    @Column(name = "code_activation_expire_le")
    private LocalDateTime codeActivationExpireLe;

    @Column(name = "employe_nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "employe_prenom", nullable = false, length = 100)
    private String prenom;

    @Column(name = "employe_email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "employe_telephone", length = 30)
    private String telephone;

    @Column(name = "employe_adresse", length = 255)
    private String adresse;

    @Column(name = "employe_date_naissance")
    private LocalDate dateNaissance;

    @Column(name = "employe_date_embauche")
    private LocalDate dateEmbauche;

    @Column(name = "derniere_acquisition_conge")
    private LocalDate derniereAcquisitionConge;

    @Column(name = "employe_sexe", length = 30)
    private String sexe;

    @Column(name = "photo_profil", length = 500)
    private String photoProfil;

    @Column(name = "role", nullable = false, length = 30)
    private String role;

    @Column(name = "statut", nullable = false, length = 30)
    private String statut;

    @Column(name = "actif", nullable = false)
    private Boolean actif;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poste_id")
    private Poste poste;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_contrat_id")
    private TypeContrat typeContrat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employe manager;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "date_modification")
    private LocalDateTime updatedAt;
}

