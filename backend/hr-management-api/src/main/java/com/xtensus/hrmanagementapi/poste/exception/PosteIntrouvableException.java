package com.xtensus.hrmanagementapi.poste.exception;

public class PosteIntrouvableException extends RuntimeException {

    public PosteIntrouvableException(Long id) {
        super("Poste introuvable avec l'identifiant " + id);
    }
}
