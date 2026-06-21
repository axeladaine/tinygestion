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
@Table(name = "journal_securite")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class JournalSecurite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    private String email;

    @Column(name = "type_evenement", nullable = false)
    private String typeEvenement; // CONNEXION_REUSSIE, CONNEXION_ECHOUEE, DECONNEXION, MOT_DE_PASSE_MODIFIE, EXPORT_DONNEES

    @Column(name = "adresse_ip")
    private String adresseIp;

    @Column(name = "date_evenement")
    private LocalDateTime dateEvenement;

    private String details;

    @PrePersist
    protected void onCreate() {
        dateEvenement = LocalDateTime.now();
    }
}
