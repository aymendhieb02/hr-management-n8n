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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CongeDemandeService {
    private static final String STATUT_EN_ATTENTE = "EN_ATTENTE";
    private static final String STATUT_APPROUVEE = "APPROUVEE";
    private static final String STATUT_REFUSEE = "REFUSEE";

    private final CongeDemandeRepository demandeRepository;
    private final EmployeRepository employeRepository;
    private final CongeTypeRepository congeTypeRepository;
    private final CongeDemandeStatutRepository statutRepository;
    private final CongeDemandeMapper mapper;

    public CongeDemandeService(CongeDemandeRepository demandeRepository, EmployeRepository employeRepository,
            CongeTypeRepository congeTypeRepository, CongeDemandeStatutRepository statutRepository, CongeDemandeMapper mapper) {
        this.demandeRepository = demandeRepository;
        this.employeRepository = employeRepository;
        this.congeTypeRepository = congeTypeRepository;
        this.statutRepository = statutRepository;
        this.mapper = mapper;
    }

    @Transactional
    public CongeDemandeResponse creer(CongeDemandeCreationRequest request) {
        Employe employe = employe(request.getEmployeId());
        if (!Boolean.TRUE.equals(employe.getActif())) {
            throw new CongeDemandeInvalideException("Un employe inactif ne peut pas soumettre une demande de conge");
        }
        CongeType type = congeType(request.getCongeTypeId());
        validerDates(request.getDateDebut(), request.getDateFin());
        LocalDateTime now = LocalDateTime.now();
        CongeDemande demande = new CongeDemande();
        demande.setEmploye(employe);
        demande.setDecideur(employe.getManager());
        demande.setCongeType(type);
        demande.setStatut(statut(STATUT_EN_ATTENTE));
        demande.setDateDebut(request.getDateDebut());
        demande.setHeureDebut(request.getHeureDebut());
        demande.setDateFin(request.getDateFin());
        demande.setHeureFin(request.getHeureFin());
        demande.setDateSoumission(now);
        demande.setNombreJours(nombreJours(request.getDateDebut(), request.getDateFin()));
        demande.setCommentaireEmploye(trim(request.getCommentaireEmploye()));
        demande.setDateCreation(now);
        return mapper.toResponse(demandeRepository.save(demande));
    }

    @Transactional
    public CongeDemandeResponse modifier(Long id, CongeDemandeModificationRequest request) {
        CongeDemande demande = entite(id);
        assurerEnAttente(demande, "Seule une demande en attente peut etre modifiee");
        validerDates(request.getDateDebut(), request.getDateFin());
        demande.setCongeType(congeType(request.getCongeTypeId()));
        demande.setDateDebut(request.getDateDebut());
        demande.setHeureDebut(request.getHeureDebut());
        demande.setDateFin(request.getDateFin());
        demande.setHeureFin(request.getHeureFin());
        demande.setNombreJours(nombreJours(request.getDateDebut(), request.getDateFin()));
        demande.setCommentaireEmploye(trim(request.getCommentaireEmploye()));
        demande.setDateModification(LocalDateTime.now());
        return mapper.toResponse(demandeRepository.save(demande));
    }

    @Transactional
    public void supprimer(Long id) {
        CongeDemande demande = entite(id);
        assurerEnAttente(demande, "Seule une demande en attente peut etre supprimee");
        demandeRepository.delete(demande);
    }

    @Transactional
    public CongeDemandeResponse approuver(Long id, CongeDecisionRequest request) {
        CongeDemande demande = entite(id);
        appliquerDecision(demande, request, STATUT_APPROUVEE, false);
        return mapper.toResponse(demandeRepository.save(demande));
    }

    @Transactional
    public CongeDemandeResponse refuser(Long id, CongeDecisionRequest request) {
        if (trim(request.getCommentaire()) == null) {
            throw new CongeDemandeInvalideException("Le commentaire est obligatoire pour refuser une demande");
        }
        CongeDemande demande = entite(id);
        appliquerDecision(demande, request, STATUT_REFUSEE, true);
        return mapper.toResponse(demandeRepository.save(demande));
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

    private void assurerEnAttente(CongeDemande demande, String message) {
        if (demande.getStatut() == null || !STATUT_EN_ATTENTE.equals(demande.getStatut().getLibelle())) throw new DecisionCongeNonAutoriseeException(message);
    }

    private void validerDates(LocalDate debut, LocalDate fin) {
        if (debut.isAfter(fin)) throw new CongeDemandeInvalideException("La date de debut ne peut pas etre apres la date de fin");
        if (debut.isBefore(LocalDate.now())) throw new CongeDemandeInvalideException("La date de debut ne peut pas etre dans le passe");
    }
    private BigDecimal nombreJours(LocalDate debut, LocalDate fin) { return BigDecimal.valueOf(ChronoUnit.DAYS.between(debut, fin) + 1); }
    private String trim(String value) { if (value == null) return null; String t = value.trim(); return t.isEmpty() ? null : t; }
}
