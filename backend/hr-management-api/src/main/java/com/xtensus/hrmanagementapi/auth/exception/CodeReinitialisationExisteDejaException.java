package com.xtensus.hrmanagementapi.auth.exception;
public class CodeReinitialisationExisteDejaException extends RuntimeException {
    public CodeReinitialisationExisteDejaException() {
        super("Un code de verification a deja ete envoye et reste valide. Consultez votre email ou attendez son expiration.");
    }
}
