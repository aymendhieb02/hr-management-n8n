package com.xtensus.hrmanagementapi.conge.demande.service;

import com.xtensus.hrmanagementapi.conge.demande.dto.CongeDecisionRequest;
import com.xtensus.hrmanagementapi.conge.demande.dto.CongeDemandeCreationRequest;
import com.xtensus.hrmanagementapi.conge.demande.dto.CongeDemandeModificationRequest;
import com.xtensus.hrmanagementapi.conge.demande.dto.CongeDemandeResponse;
import com.xtensus.hrmanagementapi.conge.demande.exception.CongeDemandeIntrouvableException;
import com.xtensus.hrmanagementapi.conge.demande.exception.CongeDemandeInvalideException;
import com.xtensus.hrmanagementapi.conge.demande.exception.DecisionCongeNonAutoriseeException;
import com.xtensus.hrmanagementapi.conge.demande.mapper.CongeDemandeMapper;
import com.xtensus.hrmanagementapi.conge.type.exception.CongeTypeIntrouvableException;
import com.xtensus.hrmanagementapi.domain.entity.CongeDemande;
import com.xtensus.hrmanagementapi.domain.entity.CongeDemandeStatut;
import com.xtensus.hrmanagementapi.domain.entity.CongeType;
import com.xtensus.hrmanagementapi.domain.entity.Employe;
import com.xtensus.hrmanagementapi.employe.exception.EmployeIntrouvableException;
import com.xtensus.hrmanagementapi.repository.CongeDemandeRepository;
import com.xtensus.hrmanagementapi.repository.CongeDemandeStatutRepository;
import com.xtensus.hrmanagementapi.repository.CongeTypeRepository;
import com.xtensus.hrmanagementapi.repository.EmployeRepository;
import com.xtensus.hrmanagementapi.repository.RaisonRepository;
import com.xtensus.hrmanagementapi.domain.entity.Raison;
import com.xtensus.hrmanagementapi.conge.demande.historique.CongeDemandeHistoriqueService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CongeDemandeService {
    private static final String STATUT_EN_ATTENTE = "EN_ATTENTE";
    private static final String STATUT_APPROUVEE = "APPROUVEE";
    private static final String STATUT_REFUSEE = "REFUSEE";
    private static final String STATUT_ANNULEE = "ANNULEE";

    private final CongeDemandeRepository demandeRepository;
    private final EmployeRepository employeRepository;
    private final CongeTypeRepository congeTypeRepository;
    private final CongeDemandeStatutRepository statutRepository;
    private final RaisonRepository raisonRepository;
    private final CongeDemandeMapper mapper;
    private final CongeDemandeHistoriqueService historiqueService;

    public CongeDemandeService(CongeDemandeRepository demandeRepository, EmployeRepository employeRepository,
            CongeTypeRepository congeTypeRepository, CongeDemandeStatutRepository statutRepository,
            RaisonRepository raisonRepository, CongeDemandeMapper mapper,
            CongeDemandeHistoriqueService historiqueService) {
        this.demandeRepository = demandeRepository;
        this.employeRepository = employeRepository;
        this.congeTypeRepository = congeTypeRepository;
        this.statutRepository = statutRepository;
        this.raisonRepository = raisonRepository;
        this.mapper = mapper;
        this.historiqueService = historiqueService;
    }

    @Transactional
    public CongeDemandeResponse creer(CongeDemandeCreationRequest request) {
        Employe employe = employe(request.getEmployeId());
        if (!Boolean.TRUE.equals(employe.getActif())) {
            throw new CongeDemandeInvalideException("Un employe inactif ne peut pas soumettre une demande de conge");
        }
        CongeType type = congeType(request.getCongeTypeId());
        validerTypeNature(type, request.getNature());
        validerPeriode(request.getNature(), request.getDateDebut(), request.getDateFin(), request.getHeureDebut(), request.getHeureFin());
        validerAbsenceDeChevauchement(employe.getId(), null, request.getDateDebut(), request.getDateFin());
        LocalDateTime now = LocalDateTime.now();
        CongeDemande demande = new CongeDemande();
        demande.setEmploye(employe);
        demande.setDecideur(employe.getManager());
        demande.setCongeType(type);
        demande.setNature(nature(request.getNature()));
        demande.setRaison(raison(request.getRaisonId(), request.getAutreMotif()));
        demande.setStatut(statut(STATUT_EN_ATTENTE));
        demande.setDateDebut(request.getDateDebut());
        demande.setHeureDebut(request.getHeureDebut());
        demande.setDateFin(request.getDateFin());
        demande.setHeureFin(request.getHeureFin());
        demande.setDateSoumission(now);
        demande.setNombreJours(request.getNombreJours() != null ? request.getNombreJours() : nombreJours(request.getDateDebut(), request.getDateFin()));
        demande.setCommentaireEmploye(trim(request.getCommentaireEmploye()));
        demande.setDateCreation(now);
        CongeDemande saved = demandeRepository.save(demande);
        historiqueService.enregistrer(saved, "CREATION", null, STATUT_EN_ATTENTE, saved.getCommentaireEmploye());
        return mapper.toResponse(saved);
    }

    @Transactional
    public CongeDemandeResponse modifier(Long id, CongeDemandeModificationRequest request) {
        CongeDemande demande = entite(id);
        assurerEnAttente(demande, "Seule une demande en attente peut etre modifiee");
        validerPeriode(request.getNature(), request.getDateDebut(), request.getDateFin(), request.getHeureDebut(), request.getHeureFin());
        validerAbsenceDeChevauchement(demande.getEmploye().getId(), demande.getId(), request.getDateDebut(), request.getDateFin());
        CongeType type = congeType(request.getCongeTypeId());
        validerTypeNature(type, request.getNature());
        demande.setCongeType(type);
        demande.setNature(nature(request.getNature()));
        demande.setRaison(raison(request.getRaisonId(), request.getAutreMotif()));
        demande.setDateDebut(request.getDateDebut());
        demande.setHeureDebut(request.getHeureDebut());
        demande.setDateFin(request.getDateFin());
        demande.setHeureFin(request.getHeureFin());
        demande.setNombreJours(request.getNombreJours() != null ? request.getNombreJours() : nombreJours(request.getDateDebut(), request.getDateFin()));
        demande.setCommentaireEmploye(trim(request.getCommentaireEmploye()));
        demande.setDateModification(LocalDateTime.now());
        CongeDemande saved = demandeRepository.save(demande);
        historiqueService.enregistrer(saved, "MODIFICATION", STATUT_EN_ATTENTE, STATUT_EN_ATTENTE, saved.getCommentaireEmploye());
        return mapper.toResponse(saved);
    }

    @Transactional
    public void supprimer(Long id) {
        CongeDemande demande = entite(id);
        assurerEnAttente(demande, "Seule une demande en attente peut etre supprimee");
        demande.setStatut(statut(STATUT_ANNULEE));
        demande.setDateModification(LocalDateTime.now());
        CongeDemande saved = demandeRepository.save(demande);
        historiqueService.enregistrer(saved, "ANNULATION", STATUT_EN_ATTENTE, STATUT_ANNULEE, null);
    }

    @Transactional
    public CongeDemandeResponse approuver(Long id, CongeDecisionRequest request) {
        CongeDemande demande = entite(id);
        String ancienStatut = demande.getStatut().getLibelle();
        appliquerDecision(demande, request, STATUT_APPROUVEE, false);
        CongeDemande saved = demandeRepository.save(demande);
        historiqueService.enregistrer(saved, "APPROBATION", ancienStatut, STATUT_APPROUVEE, request.getCommentaire());
        return mapper.toResponse(saved);
    }

    @Transactional
    public CongeDemandeResponse refuser(Long id, CongeDecisionRequest request) {
        if (trim(request.getCommentaire()) == null) {
            throw new CongeDemandeInvalideException("Le commentaire est obligatoire pour refuser une demande");
        }
        CongeDemande demande = entite(id);
        String ancienStatut = demande.getStatut().getLibelle();
        appliquerDecision(demande, request, STATUT_REFUSEE, true);
        CongeDemande saved = demandeRepository.save(demande);
        historiqueService.enregistrer(saved, "REFUS", ancienStatut, STATUT_REFUSEE, request.getCommentaire());
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CongeDemandeResponse trouverParId(Long id) { return mapper.toResponse(entite(id)); }

    @Transactional(readOnly = true)
    public List<CongeDemandeResponse> lister() { return demandeRepository.findAll().stream().map(mapper::toResponse).toList(); }

    @Transactional(readOnly = true)
    public List<CongeDemandeResponse> parEmploye(Long employeId) { return demandeRepository.findByEmployeIdOrderByDateSoumissionDesc(employeId).stream().map(mapper::toResponse).toList(); }

    @Transactional(readOnly = true)
    public List<CongeDemandeResponse> parDecideur(Long decideurId) { return demandeRepository.findByDecideurIdOrderByDateSoumissionDesc(decideurId).stream().map(mapper::toResponse).toList(); }

    private void appliquerDecision(CongeDemande demande, CongeDecisionRequest request, String nouveauStatut, boolean commentaireObligatoire) {
        assurerEnAttente(demande, "Seule une demande en attente peut recevoir une decision");
        Employe decideur = employe(request.getDecideurId());
        if (demande.getDecideur() == null || !demande.getDecideur().getId().equals(decideur.getId())) {
            throw new DecisionCongeNonAutoriseeException("Seul le decideur assigne peut traiter cette demande");
        }
        String commentaire = trim(request.getCommentaire());
        if (commentaireObligatoire && commentaire == null) {
            throw new CongeDemandeInvalideException("Le commentaire est obligatoire");
        }
        demande.setStatut(statut(nouveauStatut));
        demande.setCommentaireDecision(commentaire);
        demande.setDateDecision(LocalDateTime.now());
        demande.setDateModification(LocalDateTime.now());
    }

    private CongeDemande entite(Long id) { return demandeRepository.findById(id).orElseThrow(() -> new CongeDemandeIntrouvableException(id)); }
    private Employe employe(Long id) { return employeRepository.findById(id).orElseThrow(() -> new EmployeIntrouvableException(id)); }
    private CongeType congeType(Long id) { return congeTypeRepository.findById(id).orElseThrow(() -> new CongeTypeIntrouvableException(id)); }
    private CongeDemandeStatut statut(String libelle) { return statutRepository.findByLibelle(libelle).orElseThrow(() -> new CongeDemandeInvalideException("Statut de demande manquant: " + libelle)); }

    private String nature(String value) {
        if ("CONGE".equals(value) || "AUTORISATION_ABSENCE".equals(value)) return value;
        throw new CongeDemandeInvalideException("Nature de demande invalide");
    }

    private void validerTypeNature(CongeType type, String nature) {
        boolean autorisation = type.getNom() != null && type.getNom().toLowerCase().contains("autorisation");
        if (autorisation != "AUTORISATION_ABSENCE".equals(nature)) {
            throw new CongeDemandeInvalideException("Le type de conge ne correspond pas a la periode demandee");
        }
    }

    private Raison raison(Long raisonId, String autreMotif) {
        String libre = trim(autreMotif);
        if (libre != null) {
            if (libre.length() > 255) throw new CongeDemandeInvalideException("Le motif ne doit pas depasser 255 caracteres");
            Raison raison = new Raison();
            raison.setCommentaire(libre);
            raison.setDisponible(false);
            raison.setDateCreation(LocalDateTime.now());
            return raisonRepository.save(raison);
        }
        if (raisonId == null) throw new CongeDemandeInvalideException("La raison est obligatoire");
        Raison raison = raisonRepository.findById(raisonId).orElseThrow(() -> new CongeDemandeInvalideException("Raison introuvable"));
        if (!Boolean.TRUE.equals(raison.getDisponible())) throw new CongeDemandeInvalideException("Cette raison n'est pas disponible");
        return raison;
    }

    private void assurerEnAttente(CongeDemande demande, String message) {
        if (demande.getStatut() == null || !STATUT_EN_ATTENTE.equals(demande.getStatut().getLibelle())) throw new DecisionCongeNonAutoriseeException(message);
    }

    private void validerDates(LocalDate debut, LocalDate fin) {
        if (debut.isAfter(fin)) throw new CongeDemandeInvalideException("La date de debut ne peut pas etre apres la date de fin");
        LocalDate premiereDateAutorisee = LocalDate.now().plusDays(1);
        if (debut.isBefore(premiereDateAutorisee)) {
            throw new CongeDemandeInvalideException(
                    "La demande doit etre deposee au moins 24 heures a l'avance. Premiere date autorisee : "
                            + premiereDateAutorisee
            );
        }
    }

    private void validerAbsenceDeChevauchement(Long employeId, Long demandeId, LocalDate debut, LocalDate fin) {
        if (demandeRepository.existsChevauchementActif(employeId, demandeId, debut, fin)) {
            throw new CongeDemandeInvalideException(
                    "Une demande en attente ou approuvee existe deja sur tout ou partie de cette periode"
            );
        }
    }
    private void validerPeriode(String nature, LocalDate debut, LocalDate fin, java.time.LocalTime heureDebut, java.time.LocalTime heureFin) {
        validerDates(debut, fin);
        if ("AUTORISATION_ABSENCE".equals(nature)) {
            if (!debut.equals(fin)) throw new CongeDemandeInvalideException("Une autorisation d'absence doit concerner une seule journee");
            if (heureDebut == null || heureFin == null) throw new CongeDemandeInvalideException("Les heures de debut et de fin sont obligatoires");
            if (heureDebut.isBefore(java.time.LocalTime.of(8, 30)) || heureFin.isAfter(java.time.LocalTime.of(18, 0))) {
                throw new CongeDemandeInvalideException("L'autorisation doit etre comprise entre 08:30 et 18:00");
            }
            long minutes = Duration.between(heureDebut, heureFin).toMinutes();
            if (minutes <= 0 || minutes > 120) throw new CongeDemandeInvalideException("L'autorisation d'absence doit durer entre 1 minute et 2 heures");
        }
    }
    private BigDecimal nombreJours(LocalDate debut, LocalDate fin) { return BigDecimal.valueOf(ChronoUnit.DAYS.between(debut, fin) + 1); }
    private String trim(String value) { if (value == null) return null; String t = value.trim(); return t.isEmpty() ? null : t; }
}
