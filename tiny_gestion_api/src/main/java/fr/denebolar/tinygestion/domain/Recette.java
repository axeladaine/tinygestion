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
@Table(name = "recette")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Recette {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logement_id", nullable = false)
    private Logement logement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Plateforme plateforme;

    @Column(name = "nom_client")
    private String nomClient;

    @Column(name = "date_debut_sejour")
    private LocalDate dateDebutSejour;

    @Column(name = "date_fin_sejour")
    private LocalDate dateFinSejour;

    @Column(name = "date_encaissement")
    private LocalDate dateEncaissement;

    @Column(name = "nombre_nuits")
    private Integer nombreNuits;

    @Column(name = "montant_brut", nullable = false)
    private BigDecimal montantBrut;

    @Column(name = "frais_plateforme")
    private BigDecimal fraisPlateforme = BigDecimal.ZERO;

    @Column(name = "montant_taxe_sejour")
    private BigDecimal montantTaxeSejour = BigDecimal.ZERO;

    @Column(name = "montant_net_recu", nullable = false)
    private BigDecimal montantNetRecu;

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
        calculerNet();
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
        calculerNet();
    }

    private void calculerNet() {
        BigDecimal brut = this.montantBrut != null ? this.montantBrut : BigDecimal.ZERO;
        BigDecimal frais = this.fraisPlateforme != null ? this.fraisPlateforme : BigDecimal.ZERO;
        BigDecimal taxe = this.montantTaxeSejour != null ? this.montantTaxeSejour : BigDecimal.ZERO;
        this.montantNetRecu = brut.subtract(frais).subtract(taxe);
    }
}
