package com.xtensus.hrmanagementapi.conge.solde.service;

import com.xtensus.hrmanagementapi.repository.EmployeRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CongeSoldeInitialisation implements ApplicationRunner {
    private final EmployeRepository employes;
    private final CongeSoldeTransactionService transactions;
    public CongeSoldeInitialisation(EmployeRepository employes, CongeSoldeTransactionService transactions) {
        this.employes=employes; this.transactions=transactions;
    }
    @Override public void run(ApplicationArguments args) {
        employes.findByActifTrue().forEach(transactions::assurerSolde);
    }
}
