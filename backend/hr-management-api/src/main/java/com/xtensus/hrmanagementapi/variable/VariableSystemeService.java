package com.xtensus.hrmanagementapi.variable;

import com.xtensus.hrmanagementapi.domain.entity.VariableSysteme;
import com.xtensus.hrmanagementapi.repository.VariableSystemeRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VariableSystemeService {
    private final VariableSystemeRepository repository;
    public VariableSystemeService(VariableSystemeRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public List<VariableSysteme> lister() { return repository.findAll(); }

    public boolean estSecret(VariableSysteme variable) { return "SECRET".equalsIgnoreCase(variable.getType()); }

    @Transactional
    public VariableSysteme modifier(Long id, String valeur) {
        VariableSysteme variable = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Parametre systeme introuvable"));
        valider(variable, valeur);
        variable.setValeur(estSecret(variable) ? valeur.replace(" ", "").trim() : valeur.trim());
        VariableSysteme saved = repository.save(variable);
        if ("solde_negatif".equals(variable.getNom()) && !booleenTexte(variable.getValeur())) {
            repository.findByNom("solde_negatif_max").ifPresent(limite -> { limite.setValeur("0"); repository.save(limite); });
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public String valeur(String nom, String defaut) {
        return repository.findByNom(nom).map(VariableSysteme::getValeur).orElse(defaut);
    }
    public BigDecimal decimal(String nom, String defaut) { return new BigDecimal(valeur(nom, defaut)); }
    public int entier(String nom, int defaut) { return Integer.parseInt(valeur(nom, String.valueOf(defaut))); }
    public boolean booleen(String nom, boolean defaut) {
        String v = valeur(nom, defaut ? "OUI" : "NON");
        return "OUI".equalsIgnoreCase(v) || "TRUE".equalsIgnoreCase(v) || "1".equals(v);
    }
    private boolean booleenTexte(String valeur) { return "OUI".equalsIgnoreCase(valeur) || "TRUE".equalsIgnoreCase(valeur) || "1".equals(valeur); }

    private void valider(VariableSysteme variable, String valeur) {
        if (valeur == null || valeur.trim().isEmpty()) throw new IllegalArgumentException("La valeur est obligatoire");
        String v = valeur.trim();
        try {
            if ("DECIMAL".equalsIgnoreCase(variable.getType())) new BigDecimal(v);
            if ("INT".equalsIgnoreCase(variable.getType()) && Integer.parseInt(v) < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) { throw new IllegalArgumentException("La valeur ne correspond pas au type " + variable.getType()); }
        if ("BOOLEEN".equalsIgnoreCase(variable.getType()) && !"OUI".equalsIgnoreCase(v) && !"NON".equalsIgnoreCase(v)) {
            throw new IllegalArgumentException("Une valeur booleenne doit etre OUI ou NON");
        }
        if ("EMAIL".equalsIgnoreCase(variable.getType()) && !v.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("L'adresse email de l'expediteur est invalide");
        }
    }
}
