package fr.denebolar.tinygestion.config;

import fr.denebolar.tinygestion.domain.*;
import fr.denebolar.tinygestion.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final ProprietaireRepository proprietaireRepository;
    private final LogementRepository logementRepository;
    private final RecetteRepository recetteRepository;
    private final DepenseRepository depenseRepository;
    private final BienAmortissableRepository bienAmortissableRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        Proprietaire proprietaire = initProprietaireDemo();
        Logement logement = initLogementDemo(proprietaire);
        initUtilisateurDemo(proprietaire);
        initMockRecettesEtDepenses(logement);
        initMockBiensAmortissables(logement);
    }

    private Proprietaire initProprietaireDemo() {
        return proprietaireRepository.findByEmail("test@test.com").orElseGet(() -> {
            Proprietaire proprietaire = Proprietaire.builder()
                    .prenom("Proprio")
                    .nom("Test")
                    .email("test@test.com")
                    .telephone("0601020304")
                    .siret("12345678901234")
                    .build();
            log.info("Création du propriétaire de test par défaut.");
            return proprietaireRepository.save(proprietaire);
        });
    }

    private Logement initLogementDemo(Proprietaire proprietaire) {
        if (logementRepository.count() == 0) {
            Logement logement = Logement.builder()
                    .proprietaire(proprietaire)
                    .nom("Ma Tiny House de Rêve")
                    .adresse("123 Chemin du Ruisseau")
                    .codePostal("33000")
                    .ville("Bordeaux")
                    .qualificationLogement(QualificationLogement.RESIDENCE_SECONDAIRE)
                    .estDeplacable(true)
                    .estSurTerrainResidencePrincipale(true)
                    .estLoueCourteDuree(true)
                    .estMeubleTourisme(false)
                    .dateDebutLocation(LocalDate.now().minusMonths(6))
                    .build();
            log.info("Création du logement de test par défaut.");
            return logementRepository.save(logement);
        }
        return logementRepository.findAll().get(0);
    }

    private void initUtilisateurDemo(Proprietaire proprietaire) {
        utilisateurRepository.findByEmail("test@test.com").ifPresentOrElse(
            user -> {
                user.setMotDePasseHash(passwordEncoder.encode("password"));
                user.setProprietaire(proprietaire);
                utilisateurRepository.save(user);
                log.info("Utilisateur de test lié au propriétaire de test.");
            },
            () -> {
                Utilisateur user = Utilisateur.builder()
                        .prenom("Proprio")
                        .nom("Test")
                        .email("test@test.com")
                        .motDePasseHash(passwordEncoder.encode("password"))
                        .role(Role.PROPRIETAIRE)
                        .proprietaire(proprietaire)
                        .actif(true)
                        .build();
                utilisateurRepository.save(user);
                log.info("Créateur utilisateur de test lié au propriétaire.");
            }
        );
    }

    private void initMockRecettesEtDepenses(Logement logement) {
        if (recetteRepository.count() == 0 && depenseRepository.count() == 0) {
            LocalDate today = LocalDate.now();

            // Recette 1 (Ce mois-ci)
            Recette r1 = Recette.builder()
                    .logement(logement)
                    .plateforme(Plateforme.AIRBNB)
                    .nomClient("Jean Dupont")
                    .dateDebutSejour(today.minusDays(5))
                    .dateFinSejour(today.minusDays(2))
                    .dateEncaissement(today.minusDays(1))
                    .nombreNuits(3)
                    .montantBrut(BigDecimal.valueOf(350.00))
                    .fraisPlateforme(BigDecimal.valueOf(10.50))
                    .montantTaxeSejour(BigDecimal.valueOf(6.00))
                    .commentaire("Super séjour, client très propre.")
                    .build();

            // Recette 2 (Ce mois-ci)
            Recette r2 = Recette.builder()
                    .logement(logement)
                    .plateforme(Plateforme.BOOKING)
                    .nomClient("Marie Martin")
                    .dateDebutSejour(today.minusDays(15))
                    .dateFinSejour(today.minusDays(10))
                    .dateEncaissement(today.minusDays(8))
                    .nombreNuits(5)
                    .montantBrut(BigDecimal.valueOf(600.00))
                    .fraisPlateforme(BigDecimal.valueOf(90.00))
                    .montantTaxeSejour(BigDecimal.valueOf(10.00))
                    .commentaire("Demande d'arrivée tardive.")
                    .build();

            // Recette 3 (Le mois dernier)
            Recette r3 = Recette.builder()
                    .logement(logement)
                    .plateforme(Plateforme.DIRECT)
                    .nomClient("Famille Mercier")
                    .dateDebutSejour(today.minusMonths(1).withDayOfMonth(5))
                    .dateFinSejour(today.minusMonths(1).withDayOfMonth(12))
                    .dateEncaissement(today.minusMonths(1).withDayOfMonth(12))
                    .nombreNuits(7)
                    .montantBrut(BigDecimal.valueOf(800.00))
                    .fraisPlateforme(BigDecimal.ZERO)
                    .montantTaxeSejour(BigDecimal.valueOf(14.00))
                    .commentaire("Location directe en famille.")
                    .build();

            recetteRepository.save(r1);
            recetteRepository.save(r2);
            recetteRepository.save(r3);

            // Dépense 1 (Ce mois-ci)
            Depense d1 = Depense.builder()
                    .logement(logement)
                    .dateDepense(today.minusDays(12))
                    .fournisseur("EDF")
                    .categorie("Électricité et eau")
                    .montantTtc(BigDecimal.valueOf(120.00))
                    .tauxAffectationLocation(BigDecimal.valueOf(30.00)) // 30% affectation -> 36.00 € retenus
                    .moyenPaiement("Prélèvement automatique")
                    .statutDeductibilite(StatutDeductibilite.DEDUCTIBLE)
                    .commentaire("Facture électricité mai 2026.")
                    .build();

            // Dépense 2 (Ce mois-ci)
            Depense d2 = Depense.builder()
                    .logement(logement)
                    .dateDepense(today.minusDays(4))
                    .fournisseur("Super U")
                    .categorie("Consommables et accueil")
                    .montantTtc(BigDecimal.valueOf(45.00))
                    .tauxAffectationLocation(BigDecimal.valueOf(100.00)) // 100% -> 45.00 € retenus
                    .moyenPaiement("Carte bancaire")
                    .statutDeductibilite(StatutDeductibilite.A_VERIFIER)
                    .commentaire("Achat café, sel, condiments d'accueil.")
                    .build();

            depenseRepository.save(d1);
            depenseRepository.save(d2);

            log.info("Données de test de recettes et dépenses générées.");
        }
    }

    private void initMockBiensAmortissables(Logement logement) {
        if (bienAmortissableRepository.count() == 0) {
            LocalDate today = LocalDate.now();

            // 1. Tiny House
            BienAmortissable b1 = BienAmortissable.builder()
                    .logement(logement)
                    .nom("Tiny House Ossature Bois")
                    .categorie(CategorieBien.TINY_HOUSE)
                    .dateAchat(today.minusMonths(12))
                    .dateMiseEnService(today.minusMonths(12))
                    .montantTtc(BigDecimal.valueOf(45000.00))
                    .tauxAffectationLocation(BigDecimal.valueOf(100.00))
                    .dureeAmortissementAns(15)
                    .possedeFacture(true)
                    .commentaire("Achat initial de la tiny house principale.")
                    .build();

            // 2. Terrasse
            BienAmortissable b2 = BienAmortissable.builder()
                    .logement(logement)
                    .nom("Terrasse en Pin autoclave")
                    .categorie(CategorieBien.TERRASSE)
                    .dateAchat(today.minusMonths(6))
                    .dateMiseEnService(today.minusMonths(6))
                    .montantTtc(BigDecimal.valueOf(3200.00))
                    .tauxAffectationLocation(BigDecimal.valueOf(100.00))
                    .dureeAmortissementAns(10)
                    .possedeFacture(true)
                    .commentaire("Terrasse extérieure pour les voyageurs.")
                    .build();

            bienAmortissableRepository.save(b1);
            bienAmortissableRepository.save(b2);
            log.info("Données de test de biens amortissables générées.");
        }
    }
}
