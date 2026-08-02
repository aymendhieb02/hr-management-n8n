package com.xtensus.hrmanagementapi.conge.statut.exception;

public class CongeDemandeStatutExisteDejaException extends RuntimeException {

    public CongeDemandeStatutExisteDejaException(String libelle) {
        super("Un statut de demande de conge existe deja avec le libelle '" + libelle + "'");
    }
}
