package com.xtensus.hrmanagementapi.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "raisons")
@Getter
@Setter
@NoArgsConstructor
public class Raison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "raison_id")
    private Long id;

    @Column(name = "raison_commentaire", nullable = false, length = 255)
    private String commentaire;

    @Column(name = "raison_date_creation", nullable = false)
    private LocalDateTime dateCreation;
}
