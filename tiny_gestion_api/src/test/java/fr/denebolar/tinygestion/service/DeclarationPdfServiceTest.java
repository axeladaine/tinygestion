package fr.denebolar.tinygestion.service;

import com.lowagie.text.pdf.PdfReader;
import fr.denebolar.tinygestion.domain.*;
import fr.denebolar.tinygestion.dto.bilan.BilanFiscalDto;
import fr.denebolar.tinygestion.repository.BienAmortissableRepository;
import fr.denebolar.tinygestion.repository.DepenseRepository;
import fr.denebolar.tinygestion.repository.RecetteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeclarationPdfServiceTest {

    @Mock
    private BilanFiscalService bilanFiscalService;

    @Mock
    private DepenseRepository depenseRepository;

    @Mock
    private RecetteRepository recetteRepository;

    @Mock
    private BienAmortissableRepository bienAmortissableRepository;

    @Mock
    private LogementService logementService;

    @InjectMocks
    private DeclarationPdfService declarationPdfService;

    private Utilisateur mockUser;
    private Logement mockLogement;
    private BilanFiscalDto mockBilan;

    @BeforeEach
    void setUp() {
        mockUser = Utilisateur.builder()
                .id(1L)
                .prenom("Jean")
                .nom("Dupont")
                .email("jean@example.com")
                .build();

        mockLogement = Logement.builder()
                .id(10L)
                .nom("Tiny du Lac")
                .adresse("1 Rue du Lac")
                .codePostal("74000")
                .ville("Annecy")
                .qualificationLogement(QualificationLogement.MEUBLE_DE_TOURISME)
                .estDeplacable(true)
                .estSurTerrainResidencePrincipale(false)
                .estLoueCourteDuree(true)
                .estMeubleTourisme(true)
                .initialise(true)
                .build();

        mockBilan = new BilanFiscalDto(
                10L,
                2026,
                BigDecimal.valueOf(12000), // recettesBrutes
                BigDecimal.valueOf(3500),  // depensesRetenues
                BigDecimal.valueOf(1500),  // amortissementDisponible
                BigDecimal.valueOf(8500),  // resultatAvantAmortissement
                BigDecimal.valueOf(1500),  // amortissementUtilise
                BigDecimal.valueOf(7000),  // resultatReelImposable
                BigDecimal.valueOf(0),     // amortissementNonUtilise
                true,                      // estMeubleTourisme
                BigDecimal.valueOf(6000),  // abattementMicroBic (50%)
                BigDecimal.valueOf(6000),  // resultatMicroBicImposable
                "REGIME_REEL_AVANTAGEUX",
                BigDecimal.valueOf(1000),  // differenceGainImposable (6000 vs 7000 -> réel avantageux si on a des réductions d'impôts, ici simulation)
                true,
                "5NG"
        );
    }

    @Test
    void shouldGeneratePdfDeclarationBytesSuccessfully() {
        // Arrange
        when(logementService.getLogementById(eq(10L), eq(mockUser))).thenReturn(mockLogement);
        when(bilanFiscalService.calculerBilanFiscal(eq(10L), eq(2026), eq(mockUser))).thenReturn(mockBilan);

        List<Depense> depenses = new ArrayList<>();
        // Dépense Électricité (Achats)
        depenses.add(Depense.builder()
                .id(1L)
                .categorie("Électricité et eau")
                .montantTtc(BigDecimal.valueOf(500))
                .montantRetenu(BigDecimal.valueOf(500))
                .statutDeductibilite(StatutDeductibilite.DEDUCTIBLE)
                .dateDepense(LocalDate.of(2026, 3, 1))
                .build());
        // Dépense Assurance (Services externes)
        depenses.add(Depense.builder()
                .id(2L)
                .categorie("Assurance")
                .montantTtc(BigDecimal.valueOf(300))
                .montantRetenu(BigDecimal.valueOf(300))
                .statutDeductibilite(StatutDeductibilite.DEDUCTIBLE)
                .dateDepense(LocalDate.of(2026, 4, 1))
                .build());
        // Dépense CFE (Impôts et taxes)
        depenses.add(Depense.builder()
                .id(3L)
                .categorie("Impôts locaux (CFE, etc.)")
                .montantTtc(BigDecimal.valueOf(150))
                .montantRetenu(BigDecimal.valueOf(150))
                .statutDeductibilite(StatutDeductibilite.DEDUCTIBLE)
                .dateDepense(LocalDate.of(2026, 5, 1))
                .build());

        when(depenseRepository.findByLogementIdAndDateDepenseBetween(eq(10L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(depenses);

        List<BienAmortissable> biens = new ArrayList<>();
        biens.add(BienAmortissable.builder()
                .id(1L)
                .nom("Tiny House Bois")
                .montantTtc(BigDecimal.valueOf(45000))
                .baseAmortissable(BigDecimal.valueOf(45000))
                .amortissementAnnuel(BigDecimal.valueOf(1500))
                .dureeAmortissementAns(30)
                .dateAchat(LocalDate.of(2025, 1, 1))
                .dateMiseEnService(LocalDate.of(2025, 1, 1))
                .build());

        when(bienAmortissableRepository.findByLogementId(eq(10L))).thenReturn(biens);

        // Act
        byte[] pdfBytes = declarationPdfService.genererDeclarationPdf(10L, 2026, mockUser);
 
        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }
}
