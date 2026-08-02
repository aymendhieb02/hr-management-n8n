package com.xtensus.hrmanagementapi.typecontrat.exception;

public class TypeContratExisteDejaException extends RuntimeException {

    public TypeContratExisteDejaException(String libelle) {
        super("Un type de contrat existe deja avec le libelle '" + libelle + "'");
    }
}
