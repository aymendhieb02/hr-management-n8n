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

    @GetMapping("/{id}")
    public ResponseEntity<NotificationFrancaiseResponse> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(service.trouverParId(id));
    }

    @GetMapping("/employe/{employeId}")
    public ResponseEntity<List<NotificationFrancaiseResponse>> parEmploye(@PathVariable Long employeId) {
        return ResponseEntity.ok(service.parEmploye(employeId));
    }

    @GetMapping("/employe/{employeId}/non-lues")
    public ResponseEntity<List<NotificationFrancaiseResponse>> nonLues(@PathVariable Long employeId) {
        return ResponseEntity.ok(service.nonLues(employeId));
    }

    @PatchMapping("/{id}/lue")
    public ResponseEntity<NotificationFrancaiseResponse> marquerCommeLue(@PathVariable Long id) {
        return ResponseEntity.ok(service.marquerCommeLue(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
