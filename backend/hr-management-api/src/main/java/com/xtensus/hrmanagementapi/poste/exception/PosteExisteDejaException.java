package com.xtensus.hrmanagementapi.poste.exception;

public class PosteExisteDejaException extends RuntimeException {

    public PosteExisteDejaException(String intitule) {
        super("Un poste existe deja avec l'intitule '" + intitule + "'");
    }
}
