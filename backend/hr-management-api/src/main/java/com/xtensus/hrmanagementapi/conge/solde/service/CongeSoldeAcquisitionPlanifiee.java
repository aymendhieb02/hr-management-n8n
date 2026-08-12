package com.xtensus.hrmanagementapi.conge.solde.service;

import com.xtensus.hrmanagementapi.repository.EmployeRepository;
import java.math.BigDecimal;
import java.time.YearMonth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name="app.leave-accrual.enabled", havingValue="true", matchIfMissing=true)
public class CongeSoldeAcquisitionPlanifiee {
    private final EmployeRepository employes; private final CongeSoldeTransactionService transactions; private final BigDecimal montant;
    public CongeSoldeAcquisitionPlanifiee(EmployeRepository employes, CongeSoldeTransactionService transactions,
            @Value("${app.leave-accrual.monthly-days:2.16}") BigDecimal montant) { this.employes=employes; this.transactions=transactions; this.montant=montant; }
    @Scheduled(cron="${app.leave-accrual.cron:0 0 1 1 * *}", zone="${app.leave-accrual.zone:Africa/Tunis}")
    public void executer() {
        String mois=YearMonth.now().toString();
        employes.findByActifTrue().forEach(e -> transactions.crediterMensuellement(e,montant,"ACQUISITION:"+mois+":"+e.getId()));
    }
}
