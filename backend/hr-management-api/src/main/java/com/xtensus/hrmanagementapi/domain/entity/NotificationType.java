package com.xtensus.hrmanagementapi.domain.entity;

import jakarta.persistence.*; import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
@Entity @Table(name="notification_types") @Getter @Setter @NoArgsConstructor
public class NotificationType {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="notification_type_id") private Long id;
 @Column(name="notification_type_libelle", nullable=false, unique=true, length=100) private String libelle;
 @Column(name="notification_type_description", length=255) private String description;
 @Column(name="notification_type_actif", nullable=false) private Boolean actif;
}
