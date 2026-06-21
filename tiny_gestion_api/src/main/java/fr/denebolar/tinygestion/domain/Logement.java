package fr.denebolar.tinygestion.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "logement")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Logement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proprietaire_id", nullable = false)
    private Proprietaire proprietaire;

    @Column(nullable = false)
    private String nom;

    private String adresse;

    @Column(name = "code_postal")
    private String codePostal;

    private String ville;

    @Enumerated(EnumType.STRING)
    @Column(name = "qualification_logement", nullable = false)
    private QualificationLogement qualificationLogement;

    @Column(name = "est_deplacable")
    private boolean estDeplacable;

    @Column(name = "est_sur_terrain_residence_principale")
    private boolean estSurTerrainResidencePrincipale;

    @Column(name = "est_loue_courte_duree")
    private boolean estLoueCourteDuree = true;

    @Column(name = "est_meuble_tourisme")
    private boolean estMeubleTourisme;

    @Column(name = "date_debut_location")
    private LocalDate dateDebutLocation;

    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
}
