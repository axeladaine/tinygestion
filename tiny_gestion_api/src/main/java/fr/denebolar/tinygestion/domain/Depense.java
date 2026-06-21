package fr.denebolar.tinygestion.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "depense")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Depense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logement_id", nullable = false)
    private Logement logement;

    @Column(name = "date_depense", nullable = false)
    private LocalDate dateDepense;

    private String fournisseur;

    private String categorie;

    @Column(name = "montant_ttc", nullable = false)
    private BigDecimal montantTtc;

    @Column(name = "taux_affectation_location")
    private BigDecimal tauxAffectationLocation = BigDecimal.valueOf(100);

    @Column(name = "montant_retenu", nullable = false)
    private BigDecimal montantRetenu;

    @Column(name = "moyen_paiement")
    private String moyenPaiement;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_deductibilite", nullable = false)
    private StatutDeductibilite statutDeductibilite = StatutDeductibilite.A_VERIFIER;

    private String commentaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_justificatif_id")
    private DocumentJustificatif documentJustificatif;

    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = LocalDateTime.now();
        calculerRetenu();
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
        calculerRetenu();
    }

    private void calculerRetenu() {
        BigDecimal ttc = this.montantTtc != null ? this.montantTtc : BigDecimal.ZERO;
        BigDecimal taux = this.tauxAffectationLocation != null ? this.tauxAffectationLocation : BigDecimal.valueOf(100);
        this.montantRetenu = ttc.multiply(taux).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
