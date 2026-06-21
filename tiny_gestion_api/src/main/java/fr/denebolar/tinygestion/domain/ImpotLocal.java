package fr.denebolar.tinygestion.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "impot_local")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ImpotLocal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logement_id", nullable = false)
    private Logement logement;

    @Column(name = "type_impot_local", nullable = false)
    private String typeImpotLocal; // CFE, TAXE_HABITATION_RESIDENCE_SECONDAIRE, TAXE_FONCIERE, TAXE_AMENAGEMENT, AUTRE

    @Column(nullable = false)
    private Integer annee;

    @Column(nullable = false)
    private BigDecimal montant;

    @Column(name = "date_avis")
    private LocalDate dateAvis;

    @Column(name = "date_echeance")
    private LocalDate dateEcheance;

    @Column(name = "date_paiement")
    private LocalDate datePaiement;

    @Column(name = "statut_paiement")
    private String statutPaiement;

    @Column(name = "statut_deductibilite")
    private String statutDeductibilite;

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
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
}
