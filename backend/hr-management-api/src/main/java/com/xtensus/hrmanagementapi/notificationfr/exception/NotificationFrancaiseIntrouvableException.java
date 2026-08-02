package com.xtensus.hrmanagementapi.notificationfr.exception;

public class NotificationFrancaiseIntrouvableException extends RuntimeException {
    public NotificationFrancaiseIntrouvableException(Long id) {
        super("Notification introuvable avec l'identifiant " + id);
    }
}
