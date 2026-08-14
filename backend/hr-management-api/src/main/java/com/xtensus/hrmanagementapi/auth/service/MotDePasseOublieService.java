package com.xtensus.hrmanagementapi.auth.service;

import com.xtensus.hrmanagementapi.auth.dto.ReinitialisationMotDePasseRequest;
import com.xtensus.hrmanagementapi.auth.exception.CodeReinitialisationExisteDejaException;
import com.xtensus.hrmanagementapi.domain.entity.Employe;
import com.xtensus.hrmanagementapi.domain.entity.ReinitialisationMotDePasse;
import com.xtensus.hrmanagementapi.employe.service.CompteEmailService;
import com.xtensus.hrmanagementapi.repository.EmployeRepository;
import com.xtensus.hrmanagementapi.repository.ReinitialisationMotDePasseRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MotDePasseOublieService {
    private static final SecureRandom ALEATOIRE = new SecureRandom();
    private final EmployeRepository employes; private final ReinitialisationMotDePasseRepository codes;
    private final PasswordEncoder encoder; private final CompteEmailService emails;
    public MotDePasseOublieService(EmployeRepository employes, ReinitialisationMotDePasseRepository codes,
            PasswordEncoder encoder, CompteEmailService emails) {
        this.employes=employes; this.codes=codes; this.encoder=encoder; this.emails=emails;
    }

    @Transactional
    public DemandeCode demander(String identifiant) {
        Employe employe = trouver(identifiant);
        var precedent=codes.findFirstByEmployeIdAndUtiliseFalseOrderByDateCreationDesc(employe.getId());
        if(precedent.isPresent()) {
            var ancien=precedent.get();
            if(ancien.getDateExpiration().isAfter(LocalDateTime.now()) && ancien.getNombreTentatives()<5) throw new CodeReinitialisationExisteDejaException();
            ancien.setUtilise(true); codes.save(ancien);
        }
        String code = "%06d".formatted(ALEATOIRE.nextInt(1_000_000));
        ReinitialisationMotDePasse demande = new ReinitialisationMotDePasse();
        demande.setEmploye(employe); demande.setCodeHash(encoder.encode(code));
        demande.setDateCreation(LocalDateTime.now()); demande.setDateExpiration(LocalDateTime.now().plusMinutes(15));
        demande.setUtilise(false); demande.setNombreTentatives(0); codes.save(demande);
        emails.envoyerCodeReinitialisation(employe, code);
        return new DemandeCode(demande.getDateExpiration(), "Code envoye avec succes.");
    }

    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void reinitialiser(ReinitialisationMotDePasseRequest request) {
        Employe employe = trouver(request.identifiant());
        ReinitialisationMotDePasse demande = codes.findFirstByEmployeIdAndUtiliseFalseOrderByDateCreationDesc(employe.getId())
                .orElseThrow(() -> new IllegalArgumentException("Code invalide ou expire"));
        if (demande.getDateExpiration().isBefore(LocalDateTime.now()) || demande.getNombreTentatives() >= 5) {
            demande.setUtilise(true); codes.save(demande); throw new IllegalArgumentException("Code invalide ou expire");
        }
        if (!encoder.matches(request.code(), demande.getCodeHash())) {
            demande.setNombreTentatives(demande.getNombreTentatives()+1); codes.save(demande);
            throw new IllegalArgumentException("Code invalide ou expire");
        }
        employe.setMotDePasseHash(encoder.encode(request.nouveauMotDePasse()));
        employe.setChangementMotDePasseRequis(false); employe.setUpdatedAt(LocalDateTime.now());
        employes.save(employe); demande.setUtilise(true); codes.save(demande);
    }

    @Transactional
    public VerificationCode verifier(String identifiant, String code) {
        Employe employe = trouver(identifiant);
        var option = codes.findFirstByEmployeIdAndUtiliseFalseOrderByDateCreationDesc(employe.getId());
        if (option.isEmpty()) return new VerificationCode("EXPIRE", "Aucun code actif. Demandez un nouveau code.", 0);
        ReinitialisationMotDePasse demande=option.get();
        if (demande.getDateExpiration().isBefore(LocalDateTime.now())) {
            demande.setUtilise(true); codes.save(demande);
            return new VerificationCode("EXPIRE", "Ce code a expire. Demandez un nouveau code.", demande.getNombreTentatives());
        }
        if (demande.getNombreTentatives() >= 5) {
            demande.setUtilise(true); codes.save(demande);
            return new VerificationCode("BLOQUE", "Trop de tentatives. Demandez un nouveau code.", demande.getNombreTentatives());
        }
        if (!encoder.matches(code, demande.getCodeHash())) {
            int tentatives=demande.getNombreTentatives()+1; demande.setNombreTentatives(tentatives); codes.save(demande);
            return new VerificationCode("INVALIDE", "Code incorrect. Il vous reste " + Math.max(0,5-tentatives) + " tentative(s).", tentatives);
        }
        return new VerificationCode("VALIDE", "Code verifie avec succes.", demande.getNombreTentatives());
    }

    public record VerificationCode(String statut, String message, int tentatives) {}
    public record DemandeCode(LocalDateTime expireLe, String message) {}

    private Employe trouver(String identifiant) {
        String valeur=identifiant.trim();
        return employes.findByUsernameIgnoreCase(valeur).or(() -> employes.findByEmailIgnoreCase(valeur))
                .orElseThrow(() -> new IllegalArgumentException("Aucun compte ne correspond a cet identifiant"));
    }
}
