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
@Table(name = "bien_amortissable")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BienAmortissable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logement_id", nullable = false)
    private Logement logement;

    @Column(nullable = false)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategorieBien categorie;

    @Column(name = "date_achat", nullable = false)
    private LocalDate dateAchat;

    @Column(name = "date_mise_en_service")
    private LocalDate dateMiseEnService;

    @Column(name = "montant_ttc", nullable = false)
    private BigDecimal montantTtc;

    @Column(name = "taux_affectation_location")
    private BigDecimal tauxAffectationLocation;

    @Column(name = "base_amortissable", nullable = false)
    private BigDecimal baseAmortissable;

    @Column(name = "duree_amortissement_ans", nullable = false)
    private Integer dureeAmortissementAns;

    @Column(name = "amortissement_annuel", nullable = false)
    private BigDecimal amortissementAnnuel;

    @Column(name = "amortissement_mensuel", nullable = false)
    private BigDecimal amortissementMensuel;

    @Column(name = "amortissement_deduit_cumule")
    private BigDecimal amortissementDeduitCumule;

    @Column(name = "amortissement_non_utilise_cumule")
    private BigDecimal amortissementNonUtiliseCumule;

    @Column(name = "possede_facture")
    private Boolean possedeFacture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_justificatif_id")
    private DocumentJustificatif documentJustificatif;

    private String commentaire;

    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = LocalDateTime.now();
        calculateAmortization();
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
        calculateAmortization();
    }

    public void calculateAmortization() {
        if (this.montantTtc == null) {
            return;
        }
        if (this.tauxAffectationLocation == null) {
            this.tauxAffectationLocation = BigDecimal.valueOf(100);
        }
        
        // base_amortissable = montant_ttc * taux_affectation_location / 100
        this.baseAmortissable = this.montantTtc
                .multiply(this.tauxAffectationLocation)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        if (this.dureeAmortissementAns != null && this.dureeAmortissementAns > 0) {
            // amortissement_annuel = base_amortissable / duree_amortissement_ans
            this.amortissementAnnuel = this.baseAmortissable
                    .divide(BigDecimal.valueOf(this.dureeAmortissementAns), 2, RoundingMode.HALF_UP);
            // amortissement_mensuel = amortissement_annuel / 12
            this.amortissementMensuel = this.amortissementAnnuel
                    .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        } else {
            this.amortissementAnnuel = BigDecimal.ZERO;
            this.amortissementMensuel = BigDecimal.ZERO;
        }

        if (this.amortissementDeduitCumule == null) {
            this.amortissementDeduitCumule = BigDecimal.ZERO;
        }
        if (this.amortissementNonUtiliseCumule == null) {
            this.amortissementNonUtiliseCumule = BigDecimal.ZERO;
        }
        if (this.possedeFacture == null) {
            this.possedeFacture = false;
        }
    }
}
