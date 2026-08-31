package com.xtensus.hrmanagementapi.conge.solde.service;

import com.xtensus.hrmanagementapi.repository.EmployeRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name="app.leave-accrual.enabled", havingValue="true", matchIfMissing=true)
public class CongeSoldeAcquisitionPlanifiee {
    private final EmployeRepository employes; private final CongeSoldeTransactionService transactions;
    private final com.xtensus.hrmanagementapi.variable.VariableSystemeService variables;
    public CongeSoldeAcquisitionPlanifiee(EmployeRepository employes, CongeSoldeTransactionService transactions,
            com.xtensus.hrmanagementapi.variable.VariableSystemeService variables) { this.employes=employes; this.transactions=transactions; this.variables=variables; }
    @Scheduled(cron="${app.leave-accrual.cron:0 5 0 * * *}", zone="${app.leave-accrual.zone:Africa/Tunis}")
    public void executer() { executerPourDate(LocalDate.now()); }

    /** Exécute l'acquisition pour une date donnée; utile pour les tests et les reprises contrôlées. */
    public int executerPourDate(LocalDate aujourdHui) {
        return executerPourDate(aujourdHui, "ACQUISITION:");
    }
    public int executerPourDateTest(LocalDate aujourdHui) {
        return executerPourDate(aujourdHui, "TEST_ACQUISITION:");
    }
    public int annulerAcquisitionsTest(String prefix) {
        return transactions.annulerAcquisitionsTest(prefix);
    }
    private int executerPourDate(LocalDate aujourdHui, String prefix) {
        BigDecimal montant = variables.decimal("solde_conge_par_mois", "2.16");
        int[] compteur = {0};
        employes.findByActifTrue().stream()
                .filter(e -> e.getDateEmbauche() != null)
                .filter(e -> jourAnniversaire(e.getDateEmbauche(), aujourdHui) == aujourdHui.getDayOfMonth())
                .forEach(e -> {
                    String reference = prefix + aujourdHui.getYear() + "-" + aujourdHui.getMonthValue() + ":" + e.getId();
                    transactions.crediterMensuellement(e, montant, reference);
                    e.setDerniereAcquisitionConge(aujourdHui);
                    employes.save(e);
                    compteur[0]++;
                });
        return compteur[0];
    }
    private int jourAnniversaire(LocalDate embauche, LocalDate date) {
        return Math.min(embauche.getDayOfMonth(), date.lengthOfMonth());
    }
}
