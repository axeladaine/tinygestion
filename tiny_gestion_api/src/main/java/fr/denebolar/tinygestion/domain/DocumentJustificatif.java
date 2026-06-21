package fr.denebolar.tinygestion.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "document_justificatif")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DocumentJustificatif {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logement_id", nullable = false)
    private Logement logement;

    @Column(name = "nom_fichier", nullable = false)
    private String nomFichier;

    @Column(name = "type_fichier")
    private String typeFichier;

    @Column(name = "chemin_stockage", nullable = false)
    private String cheminStockage;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_document", nullable = false)
    private TypeDocument typeDocument;

    @Column(name = "entite_liee_type")
    private String entiteLieeType;

    @Column(name = "entite_liee_id")
    private Long entiteLieeId;

    @Column(name = "date_televersement", updatable = false)
    private LocalDateTime dateTeleversement;

    @PrePersist
    protected void onCreate() {
        dateTeleversement = LocalDateTime.now();
    }
}
