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
import com.xtensus.hrmanagementapi.repository.JourFerieRepository;
import com.xtensus.hrmanagementapi.domain.entity.Raison;
import com.xtensus.hrmanagementapi.conge.demande.historique.CongeDemandeHistoriqueService;
import com.xtensus.hrmanagementapi.notificationfr.service.NotificationFrancaiseService;
import com.xtensus.hrmanagementapi.conge.solde.service.CongeSoldeTransactionService;
import com.xtensus.hrmanagementapi.workflow.CongeWorkflowService;
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
    private static final String STATUT_BROUILLON = "BROUILLON";
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
    private final NotificationFrancaiseService notificationService;
    private final CongeSoldeTransactionService soldeTransactionService;
    private final JourFerieRepository jourFerieRepository;
    private final CongeWorkflowService workflowService;
    private final com.xtensus.hrmanagementapi.workflow.EmailOutboxService emailOutboxService;

    public CongeDemandeService(CongeDemandeRepository demandeRepository, EmployeRepository employeRepository,
            CongeTypeRepository congeTypeRepository, CongeDemandeStatutRepository statutRepository,
            RaisonRepository raisonRepository, CongeDemandeMapper mapper,
            CongeDemandeHistoriqueService historiqueService,
            NotificationFrancaiseService notificationService,
            CongeSoldeTransactionService soldeTransactionService,
            JourFerieRepository jourFerieRepository, CongeWorkflowService workflowService,
            com.xtensus.hrmanagementapi.workflow.EmailOutboxService emailOutboxService) {
        this.demandeRepository = demandeRepository;
        this.employeRepository = employeRepository;
        this.congeTypeRepository = congeTypeRepository;
        this.statutRepository = statutRepository;
        this.raisonRepository = raisonRepository;
        this.mapper = mapper;
        this.historiqueService = historiqueService;
        this.notificationService = notificationService;
        this.soldeTransactionService = soldeTransactionService;
        this.jourFerieRepository = jourFerieRepository;
        this.workflowService = workflowService;
        this.emailOutboxService = emailOutboxService;
    }

    @Transactional
    public CongeDemandeResponse creer(CongeDemandeCreationRequest request) {
        Employe employe = employe(request.getEmployeId());
        if (!Boolean.TRUE.equals(employe.getActif())) {
            throw new CongeDemandeInvalideException("Un employe inactif ne peut pas soumettre une demande de conge");
        }
        CongeType type = congeType(request.getCongeTypeId());
        validerTypeNature(type, request.getNature());
        BigDecimal nombreJours = request.getNombreJours() != null
                ? request.getNombreJours()
                : nombreJours(request.getDateDebut(), request.getDateFin());
        LocalDateTime now = LocalDateTime.now();
        CongeDemande demande = new CongeDemande();
        demande.setEmploye(employe);
        demande.setDecideur(null);
        demande.setCongeType(type);
        demande.setNature(nature(request.getNature()));
        demande.setRaison(raison(request.getRaisonId(), request.getAutreMotif()));
        demande.setStatut(statut(STATUT_BROUILLON));
        demande.setDateDebut(request.getDateDebut());
        demande.setHeureDebut(request.getHeureDebut());
        demande.setDateFin(request.getDateFin());
        demande.setHeureFin(request.getHeureFin());
        demande.setDateSoumission(null);
        demande.setNombreJours(nombreJours);
        demande.setSamediCompte(false);
        demande.setCommentaireEmploye(trim(request.getCommentaireEmploye()));
        demande.setDateCreation(now);
        CongeDemande saved = demandeRepository.save(demande);
        historiqueService.enregistrer(saved, "CREATION_BROUILLON", null, STATUT_BROUILLON, saved.getCommentaireEmploye());
        return response(saved);
    }

    @Transactional
    public CongeDemandeResponse soumettre(Long id, Long employeConnecteId) {
        CongeDemande demande = entite(id);
        assurerProprietaire(demande, employeConnecteId);
        assurerBrouillon(demande, "Seul un brouillon peut etre confirme et envoye");
        if (!Boolean.TRUE.equals(demande.getEmploye().getActif())) throw new CongeDemandeInvalideException("Un employe inactif ne peut pas soumettre une demande de conge");
        validerPeriode(demande.getNature(), demande.getDateDebut(), demande.getDateFin(), demande.getHeureDebut(), demande.getHeureFin());
        validerAbsenceDeChevauchement(demande.getEmploye().getId(), demande.getId(), demande.getDateDebut(), demande.getDateFin());
        validerSoldeAvantSoumission(demande.getEmploye(), demande.getId(), demande.getNature(), demande.getNombreJours());
        demande.setStatut(statut(STATUT_EN_ATTENTE));
        demande.setDateSoumission(LocalDateTime.now());
        demande.setDateModification(LocalDateTime.now());
        CongeDemande saved = demandeRepository.save(demande);
        workflowService.demarrer(saved);
        saved = demandeRepository.save(saved);
        historiqueService.enregistrer(saved, "SOUMISSION", STATUT_BROUILLON, STATUT_EN_ATTENTE, saved.getCommentaireEmploye());
        return response(saved);
    }

    @Transactional
    public CongeDemandeResponse modifier(Long id, CongeDemandeModificationRequest request, Long employeConnecteId) {
        CongeDemande demande = entite(id);
        assurerProprietaire(demande, employeConnecteId);
        assurerBrouillon(demande, "Seul un brouillon peut etre modifie");
        CongeType type = congeType(request.getCongeTypeId());
        validerTypeNature(type, request.getNature());
        BigDecimal nombreJours = request.getNombreJours() != null
                ? request.getNombreJours()
                : nombreJours(request.getDateDebut(), request.getDateFin());
        demande.setCongeType(type);
        demande.setNature(nature(request.getNature()));
        demande.setRaison(raison(request.getRaisonId(), request.getAutreMotif()));
        demande.setDateDebut(request.getDateDebut());
        demande.setHeureDebut(request.getHeureDebut());
        demande.setDateFin(request.getDateFin());
        demande.setHeureFin(request.getHeureFin());
        demande.setNombreJours(nombreJours);
        demande.setCommentaireEmploye(trim(request.getCommentaireEmploye()));
        demande.setDateModification(LocalDateTime.now());
        CongeDemande saved = demandeRepository.save(demande);
        historiqueService.enregistrer(saved, "MODIFICATION_BROUILLON", STATUT_BROUILLON, STATUT_BROUILLON, saved.getCommentaireEmploye());
        return response(saved);
    }

    @Transactional
    public void supprimer(Long id, Long employeConnecteId) {
        CongeDemande demande = entite(id);
        assurerProprietaire(demande, employeConnecteId);
        assurerBrouillon(demande, "Seul un brouillon peut etre supprime");
        demande.setStatut(statut(STATUT_ANNULEE));
        demande.setDateModification(LocalDateTime.now());
        CongeDemande saved = demandeRepository.save(demande);
        historiqueService.enregistrer(saved, "SUPPRESSION_BROUILLON", STATUT_BROUILLON, STATUT_ANNULEE, null);
    }

    @Transactional
    public CongeDemandeResponse approuver(Long id, CongeDecisionRequest request) {
        CongeDemande demande = entite(id);
        String ancienStatut = demande.getStatut().getLibelle();
        boolean finale = workflowService.approuver(demande, request.getDecideurId(), trim(request.getCommentaire()));
        if (!finale) {
            demande.setDateModification(LocalDateTime.now());
            CongeDemande saved = demandeRepository.save(demande);
            historiqueService.enregistrer(saved, "VALIDATION_INTERMEDIAIRE", ancienStatut, ancienStatut, request.getCommentaire());
            return response(saved);
        }
        boolean samediCompte = Boolean.TRUE.equals(request.getSamediCompte());
        demande.setSamediCompte(samediCompte);
        if (!"AUTORISATION_ABSENCE".equals(demande.getNature())) {
            demande.setNombreJours(nombreJoursOuvrables(demande.getDateDebut(), demande.getDateFin(), samediCompte));
        }
        appliquerDecisionFinale(demande, request, STATUT_APPROUVEE, false);
        soldeTransactionService.debiter(demande);
        CongeDemande saved = demandeRepository.save(demande);
        historiqueService.enregistrer(saved, "APPROBATION", ancienStatut, STATUT_APPROUVEE, request.getCommentaire());
        notificationService.notifier(
                saved.getEmploye(), "DECISION_CONGE", "Demande approuvée",
                "Votre " + libelleNature(saved) + " du " + periode(saved) + " a été approuvée."
                        + commentaireDecision(saved), "HAUTE");
        return response(saved);
    }

    @Transactional
    public CongeDemandeResponse refuser(Long id, CongeDecisionRequest request) {
        if (trim(request.getCommentaire()) == null) {
            throw new CongeDemandeInvalideException("Le commentaire est obligatoire pour refuser une demande");
        }
        CongeDemande demande = entite(id);
        String ancienStatut = demande.getStatut().getLibelle();
        workflowService.refuser(demande, request.getDecideurId(), trim(request.getCommentaire()));
        appliquerDecisionFinale(demande, request, STATUT_REFUSEE, true);
        CongeDemande saved = demandeRepository.save(demande);
        historiqueService.enregistrer(saved, "REFUS", ancienStatut, STATUT_REFUSEE, request.getCommentaire());
        notificationService.notifier(
                saved.getEmploye(), "DECISION_CONGE", "Demande refusée",
                "Votre " + libelleNature(saved) + " du " + periode(saved) + " a été refusée."
                        + commentaireDecision(saved), "HAUTE");
        return response(saved);
    }

    @Transactional
    public CongeDemandeResponse ajusterConsommation(Long id, BigDecimal joursReels, String commentaire, Long acteurId) {
        CongeDemande demande=entite(id);
        if (demande.getStatut()==null || !STATUT_APPROUVEE.equals(demande.getStatut().getLibelle())) {
            throw new CongeDemandeInvalideException("Seule une demande approuvee peut etre ajustee");
        }
        if (demande.getDateDebut().isAfter(LocalDate.now())) throw new CongeDemandeInvalideException("La consommation ne peut etre regularisee qu'apres le debut du conge");
        String commentaireNettoye=trim(commentaire); if(commentaireNettoye==null) throw new CongeDemandeInvalideException("Le commentaire de regularisation est obligatoire");
        BigDecimal ancienne=demande.getNombreJoursConsomme()==null?demande.getNombreJours():demande.getNombreJoursConsomme();
        if(joursReels!=null&&ancienne.compareTo(joursReels)==0) throw new CongeDemandeInvalideException("La consommation reelle est deja enregistree a cette valeur");
        soldeTransactionService.ajusterConsommation(demande, joursReels);
        demande.setNombreJoursConsomme(joursReels);
        demande.setDateFinReelle(calculerDateFinReelle(demande.getDateDebut(),joursReels,Boolean.TRUE.equals(demande.getSamediCompte())));
        demande.setDateRegularisation(LocalDateTime.now()); demande.setRegularisePar(employe(acteurId));
        demande.setCommentaireRegularisation(commentaireNettoye); demande.setDateModification(LocalDateTime.now());
        CongeDemande saved=demandeRepository.save(demande);
        historiqueService.enregistrer(saved,"AJUSTEMENT_CONSOMMATION",STATUT_APPROUVEE,STATUT_APPROUVEE,"Consommation reelle : "+ancienne+" -> "+joursReels+" jour(s). Regularise par "+saved.getRegularisePar().getPrenom()+" "+saved.getRegularisePar().getNom()+". "+commentaireNettoye);
        notificationService.notifier(saved.getEmploye(),"DECISION_CONGE","Conge regularise","Votre conge a ete regularise a "+joursReels+" jour(s) reellement consomme(s). Motif : "+commentaireNettoye,"NORMALE");
        emailOutboxService.planifier(saved.getEmploye(),"XTENSUS HR - Conge regularise","Votre conge a ete regularise",
                "La consommation reelle de votre conge du "+saved.getDateDebut()+" au "+saved.getDateFin()+" est maintenant de "+joursReels+" jour(s). Motif : "+commentaireNettoye);
        return response(saved);
    }

    private LocalDate calculerDateFinReelle(LocalDate debut, BigDecimal joursReels, boolean samediCompte) {
        if(joursReels==null||joursReels.signum()==0)return null;
        int jours;
        try { jours=joursReels.intValueExact(); } catch(ArithmeticException ex) { throw new CongeDemandeInvalideException("La consommation reelle doit etre un nombre entier de jours"); }
        LocalDate date=debut.minusDays(1);int comptes=0;
        while(comptes<jours){date=date.plusDays(1);if(date.getDayOfWeek()==java.time.DayOfWeek.SUNDAY)continue;if(!samediCompte&&date.getDayOfWeek()==java.time.DayOfWeek.SATURDAY)continue;if(jourFerieRepository.existsByDateAndActifTrue(date))continue;comptes++;}
        return date;
    }

    @Transactional(readOnly = true)
    public CongeDemandeResponse trouverParId(Long id, Long acteurId) { CongeDemande d=entite(id); if (STATUT_BROUILLON.equals(d.getStatut().getLibelle()) && !d.getEmploye().getId().equals(acteurId)) throw new DecisionCongeNonAutoriseeException("Ce brouillon est prive"); return response(d); }

    @Transactional(readOnly = true)
    public List<CongeDemandeResponse> lister() { return demandeRepository.findAll().stream().filter(d -> !STATUT_BROUILLON.equals(d.getStatut().getLibelle())).map(this::response).toList(); }

    @Transactional(readOnly = true)
    public List<CongeDemandeResponse> parEmploye(Long employeId, Long acteurId) { return demandeRepository.findByEmployeIdOrderByDateSoumissionDesc(employeId).stream().filter(d -> employeId.equals(acteurId) || !STATUT_BROUILLON.equals(d.getStatut().getLibelle())).map(this::response).toList(); }

    @Transactional(readOnly = true)
    public List<CongeDemandeResponse> parDecideur(Long decideurId, com.xtensus.hrmanagementapi.domain.enums.RoleType role) {
        java.util.LinkedHashMap<Long,CongeDemande> visibles=new java.util.LinkedHashMap<>();
        workflowService.demandesActives(decideurId).forEach(d->visibles.put(d.getId(),d));
        if(role==com.xtensus.hrmanagementapi.domain.enums.RoleType.DG||role==com.xtensus.hrmanagementapi.domain.enums.RoleType.DT){
            demandeRepository.findAll().stream().filter(d->d.getStatut()!=null&&STATUT_APPROUVEE.equals(d.getStatut().getLibelle())).forEach(d->visibles.put(d.getId(),d));
        }
        return visibles.values().stream().map(this::response).toList();
    }

    private CongeDemandeResponse response(CongeDemande demande) { CongeDemandeResponse r=mapper.toResponse(demande); workflowService.enrichir(r); return r; }

    private void appliquerDecisionFinale(CongeDemande demande, CongeDecisionRequest request, String nouveauStatut, boolean commentaireObligatoire) {
        assurerEnAttente(demande, "Seule une demande en attente peut recevoir une decision");
        Employe decideur = employe(request.getDecideurId());
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

    private void assurerBrouillon(CongeDemande demande, String message) {
        if (demande.getStatut() == null || !STATUT_BROUILLON.equals(demande.getStatut().getLibelle())) throw new DecisionCongeNonAutoriseeException(message);
    }

    private void assurerProprietaire(CongeDemande demande, Long employeId) {
        if (employeId == null || !demande.getEmploye().getId().equals(employeId)) throw new DecisionCongeNonAutoriseeException("Vous ne pouvez agir que sur vos propres brouillons");
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
        if (debut.getDayOfWeek() == java.time.DayOfWeek.SATURDAY
                || debut.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            throw new CongeDemandeInvalideException("La date de debut doit etre un jour ouvrable : les week-ends ne sont pas autorises");
        }
        if (jourFerieRepository.existsByDateAndActifTrue(debut)) {
            throw new CongeDemandeInvalideException("La date de debut correspond a un jour ferie et ne peut pas etre selectionnee");
        }
    }

    private void validerAbsenceDeChevauchement(Long employeId, Long demandeId, LocalDate debut, LocalDate fin) {
        if (demandeRepository.existsChevauchementActif(employeId, demandeId, debut, fin)) {
            throw new CongeDemandeInvalideException(
                    "Une demande en attente ou approuvee existe deja sur tout ou partie de cette periode"
            );
        }
    }

    private void validerSoldeAvantSoumission(Employe employe, Long demandeModifieeId, String nature, BigDecimal joursDemandes) {
        if ("AUTORISATION_ABSENCE".equals(nature)) return;
        BigDecimal joursReserves = demandeRepository.findByEmployeIdOrderByDateSoumissionDesc(employe.getId()).stream()
                .filter(d -> demandeModifieeId == null || !demandeModifieeId.equals(d.getId()))
                .filter(d -> "EN_ATTENTE".equals(d.getStatut().getLibelle()))
                .filter(d -> !"AUTORISATION_ABSENCE".equals(d.getNature()))
                .map(CongeDemande::getNombreJours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        soldeTransactionService.validerDisponibilite(employe, joursDemandes, joursReserves);
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
    private BigDecimal nombreJoursOuvrables(LocalDate debut, LocalDate fin, boolean samediCompte) {
        long total = debut.datesUntil(fin.plusDays(1)).filter(date -> {
            if (date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) return false;
            if (!samediCompte && date.getDayOfWeek() == java.time.DayOfWeek.SATURDAY) return false;
            return !jourFerieRepository.existsByDateAndActifTrue(date);
        }).count();
        return BigDecimal.valueOf(total);
    }
    private String trim(String value) { if (value == null) return null; String t = value.trim(); return t.isEmpty() ? null : t; }

    private String libelleNature(CongeDemande demande) {
        return "AUTORISATION_ABSENCE".equals(demande.getNature()) ? "autorisation d'absence" : "demande de congé";
    }

    private String periode(CongeDemande demande) {
        if ("AUTORISATION_ABSENCE".equals(demande.getNature())) {
            return demande.getDateDebut() + " de " + demande.getHeureDebut() + " à " + demande.getHeureFin();
        }
        return demande.getDateDebut() + " au " + demande.getDateFin();
    }

    private String commentaireDecision(CongeDemande demande) {
        return demande.getCommentaireDecision() == null ? "" : " Commentaire : " + demande.getCommentaireDecision();
    }
}
