package com.xtensus.hrmanagementapi.notificationfr.controller;

import com.xtensus.hrmanagementapi.notificationfr.dto.NotificationFrancaiseRequest;
import com.xtensus.hrmanagementapi.notificationfr.dto.NotificationFrancaiseResponse;
import com.xtensus.hrmanagementapi.notificationfr.service.NotificationFrancaiseService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.xtensus.hrmanagementapi.security.user.CustomUserDetails;

@RestController
@RequestMapping("/api/notifications-v2")
public class NotificationFrancaiseController {
    private final NotificationFrancaiseService service;

    public NotificationFrancaiseController(NotificationFrancaiseService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<NotificationFrancaiseResponse> creer(@Valid @RequestBody NotificationFrancaiseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(request));
    }

    @GetMapping("/me")
    public ResponseEntity<List<NotificationFrancaiseResponse>> mesNotifications(
            @AuthenticationPrincipal CustomUserDetails utilisateur) {
        return ResponseEntity.ok(service.visiblesPour(utilisateur, false));
    }

    @GetMapping("/me/non-lues")
    public ResponseEntity<List<NotificationFrancaiseResponse>> mesNotificationsNonLues(
            @AuthenticationPrincipal CustomUserDetails utilisateur) {
        return ResponseEntity.ok(service.visiblesPour(utilisateur, true));
    }

    @PatchMapping("/{id}/lue")
    public ResponseEntity<NotificationFrancaiseResponse> marquerCommeLue(
            @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails utilisateur) {
        return ResponseEntity.ok(service.marquerCommeLue(id, utilisateur));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(
            @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails utilisateur) {
        service.supprimer(id, utilisateur);
        return ResponseEntity.noContent().build();
    }
}
