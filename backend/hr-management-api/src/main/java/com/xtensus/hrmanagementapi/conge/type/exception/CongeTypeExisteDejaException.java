package com.xtensus.hrmanagementapi.conge.type.exception;

public class CongeTypeExisteDejaException extends RuntimeException {

    public CongeTypeExisteDejaException(String nom) {
        super("Un type de conge existe deja avec le nom '" + nom + "'");
    }
}
