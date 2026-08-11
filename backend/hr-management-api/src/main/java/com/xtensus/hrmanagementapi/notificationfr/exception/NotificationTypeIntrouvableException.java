package com.xtensus.hrmanagementapi.notificationfr.exception;

public class NotificationTypeIntrouvableException extends RuntimeException {
    public NotificationTypeIntrouvableException(Long id) {
        super("Type de notification introuvable avec l'identifiant " + id);
    }

    public NotificationTypeIntrouvableException(String libelle) {
        super("Type de notification introuvable : " + libelle);
    }
}
