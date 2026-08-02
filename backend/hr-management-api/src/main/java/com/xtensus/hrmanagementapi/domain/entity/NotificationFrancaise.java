package com.xtensus.hrmanagementapi.domain.entity;

import jakarta.persistence.*; import java.time.LocalDateTime; import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
@Entity @Table(name="notifications") @Getter @Setter @NoArgsConstructor
public class NotificationFrancaise {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="notification_id") private Long id;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="employe_id", nullable=false) private Employe employe;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="notification_type_id", nullable=false) private NotificationType type;
 @Column(name="notification_titre", nullable=false, length=200) private String titre;
 @Column(name="notification_contenu", nullable=false, columnDefinition="TEXT") private String contenu;
 @Column(name="notification_lu", nullable=false) private Boolean lu;
 @Column(name="notification_date_creation", nullable=false) private LocalDateTime dateCreation;
 @Column(name="notification_date_lecture") private LocalDateTime dateLecture;
 @Column(name="notification_priorite_notification", length=50) private String priorite;
}
