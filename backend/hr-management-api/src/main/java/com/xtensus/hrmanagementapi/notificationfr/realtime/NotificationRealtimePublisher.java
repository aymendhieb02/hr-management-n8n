package com.xtensus.hrmanagementapi.notificationfr.realtime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class NotificationRealtimePublisher {
    private static NotificationWebSocketHandler handler;

    public NotificationRealtimePublisher(NotificationWebSocketHandler webSocketHandler) {
        handler = webSocketHandler;
    }

    public static void publierApresCommit() {
        if (handler == null) return;
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            handler.signalerActualisation();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { handler.signalerActualisation(); }
        });
    }
}
