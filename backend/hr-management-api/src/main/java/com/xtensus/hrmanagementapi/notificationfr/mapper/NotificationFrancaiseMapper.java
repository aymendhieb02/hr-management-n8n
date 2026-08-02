package com.xtensus.hrmanagementapi.notificationfr.mapper;

import com.xtensus.hrmanagementapi.domain.entity.NotificationFrancaise;
import com.xtensus.hrmanagementapi.notificationfr.dto.NotificationFrancaiseResponse;
import org.springframework.stereotype.Component;

@Component
public class NotificationFrancaiseMapper {
    public NotificationFrancaiseResponse toResponse(NotificationFrancaise notification) {
        NotificationFrancaiseResponse response = new NotificationFrancaiseResponse();
        response.setId(notification.getId());
        response.setTitre(notification.getTitre());
        response.setContenu(notification.getContenu());
        response.setLu(notification.getLu());
        response.setDateCreation(notification.getDateCreation());
        response.setDateLecture(notification.getDateLecture());
        response.setPriorite(notification.getPriorite());
        if (notification.getEmploye() != null) {
            response.setEmployeId(notification.getEmploye().getId());
            response.setEmployeNom(notification.getEmploye().getPrenom() + " " + notification.getEmploye().getNom());
        }
        if (notification.getType() != null) {
            response.setTypeLibelle(notification.getType().getLibelle());
        }
        return response;
    }
}
