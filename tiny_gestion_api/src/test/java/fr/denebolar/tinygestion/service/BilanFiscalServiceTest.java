package fr.denebolar.tinygestion.service;

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
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BilanFiscalServiceTest {

    @Mock
    private RecetteRepository recetteRepository;

    @Mock
    private DepenseRepository depenseRepository;

    @Mock
    private BienAmortissableRepository bienAmortissableRepository;

    @Mock
    private LogementService logementService;

    @InjectMocks
    private BilanFiscalService service;

    private Utilisateur user;
    private Logement logement;

    @BeforeEach
    public void setUp() {
        user = Utilisateur.builder().id(1L).email("test@example.com").role(Role.PROPRIETAIRE).build();
        logement = Logement.builder()
                .id(10L)
                .nom("Tiny des Bois")
                .qualificationLogement(QualificationLogement.RESIDENCE_SECONDAIRE)
                .estMeubleTourisme(false)
                .build();
    }

    @Test
    public void should_calculate_correct_fiscal_arbitrage_favoring_real_regime() {
        // Arrange
        when(logementService.getLogementById(10L, user)).thenReturn(logement);

        // Recettes: 3000€
        Recette r1 = Recette.builder().montantBrut(BigDecimal.valueOf(3000.00)).build();
        when(recetteRepository.findByLogementIdAndDateEncaissementBetween(eq(10L), any(), any()))
                .thenReturn(Collections.singletonList(r1));

        // Dépenses déductibles: 1200€
        Depense d1 = Depense.builder()
                .montantRetenu(BigDecimal.valueOf(1200.00))
                .statutDeductibilite(StatutDeductibilite.DEDUCTIBLE)
                .build();
        when(depenseRepository.findByLogementIdAndDateDepenseBetween(eq(10L), any(), any()))
                .thenReturn(Collections.singletonList(d1));

        // Amortissement: climatisation 5000€ / 5 ans = 1000€
        // Date d'achat et mise en service: 2026-01-01
        BienAmortissable b1 = BienAmortissable.builder()
                .dateMiseEnService(LocalDate.of(2026, 1, 1))
                .amortissementAnnuel(BigDecimal.valueOf(1000.00))
                .dureeAmortissementAns(5)
                .build();
        when(bienAmortissableRepository.findByLogementId(10L))
                .thenReturn(Collections.singletonList(b1));

        // Act
        BilanFiscalDto dto = service.calculerBilanFiscal(10L, 2026, user);

        // Assert
        assertNotNull(dto);
        assertEquals(0, BigDecimal.valueOf(3000.00).compareTo(dto.recettesBrutes()));
        assertEquals(0, BigDecimal.valueOf(1200.00).compareTo(dto.depensesRetenues()));
        assertEquals(0, BigDecimal.valueOf(1000.00).compareTo(dto.amortissementDisponible()));
        // Résultat avant amortissement = 3000 - 1200 = 1800
        assertEquals(0, BigDecimal.valueOf(1800.00).compareTo(dto.resultatAvantAmortissement()));
        // Amortissement utilisé = min(1800, 1000) = 1000
        assertEquals(0, BigDecimal.valueOf(1000.00).compareTo(dto.amortissementUtilise()));
        // Résultat Réel imposable = 1800 - 1000 = 800
        assertEquals(0, BigDecimal.valueOf(800.00).compareTo(dto.resultatReelImposable()));
        // Amortissement non utilisé = 1000 - 1000 = 0
        assertEquals(0, BigDecimal.ZERO.compareTo(dto.amortissementNonUtilise()));

        // Micro-BIC: 50% abattement car meublé classique (RESIDENCE_SECONDAIRE)
        // Abattement = 1500, Résultat Micro-BIC imposable = 1500
        assertEquals(0, BigDecimal.valueOf(1500.00).compareTo(dto.abattementMicroBic()));
        assertEquals(0, BigDecimal.valueOf(1500.00).compareTo(dto.resultatMicroBicImposable()));

        // Arbitrage: Réel (800) < Micro-BIC (1500) -> REGIME_REEL_AVANTAGEUX
        assertEquals("REGIME_REEL_AVANTAGEUX", dto.regimeFiscalConseille());
        assertEquals(0, BigDecimal.valueOf(700.00).compareTo(dto.differenceGainImposable()));
    }

    @Test
    public void should_calculate_correct_fiscal_arbitrage_favoring_micro_bic_with_meuble_tourisme() {
        // Arrange
        logement.setQualificationLogement(QualificationLogement.MEUBLE_DE_TOURISME);
        logement.setEstMeubleTourisme(true);
        when(logementService.getLogementById(10L, user)).thenReturn(logement);

        // Recettes: 4000€
        Recette r1 = Recette.builder().montantBrut(BigDecimal.valueOf(4000.00)).build();
        when(recetteRepository.findByLogementIdAndDateEncaissementBetween(eq(10L), any(), any()))
                .thenReturn(Collections.singletonList(r1));

        // Dépenses déductibles: 100€
        Depense d1 = Depense.builder()
                .montantRetenu(BigDecimal.valueOf(100.00))
                .statutDeductibilite(StatutDeductibilite.DEDUCTIBLE)
                .build();
        when(depenseRepository.findByLogementIdAndDateDepenseBetween(eq(10L), any(), any()))
                .thenReturn(Collections.singletonList(d1));

        // Pas d'amortissement
        when(bienAmortissableRepository.findByLogementId(10L))
                .thenReturn(Collections.emptyList());

        // Act
        BilanFiscalDto dto = service.calculerBilanFiscal(10L, 2026, user);

        // Assert
        assertNotNull(dto);
        // Résultat Réel imposable = 4000 - 100 - 0 = 3900
        assertEquals(0, BigDecimal.valueOf(3900.00).compareTo(dto.resultatReelImposable()));

        // Micro-BIC: 71% abattement car meublé de tourisme
        // Abattement = 4000 * 71 / 100 = 2840
        // Résultat Micro-BIC imposable = 4000 - 2840 = 1160
        assertEquals(0, BigDecimal.valueOf(2840.00).compareTo(dto.abattementMicroBic()));
        assertEquals(0, BigDecimal.valueOf(1160.00).compareTo(dto.resultatMicroBicImposable()));

        // Arbitrage: Micro-BIC (1160) < Réel (3900) -> MICRO_BIC_AVANTAGEUX
        assertEquals("MICRO_BIC_AVANTAGEUX", dto.regimeFiscalConseille());
        assertEquals(0, BigDecimal.valueOf(2740.00).compareTo(dto.differenceGainImposable()));
    }
}
