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
                .estLoueCourteDuree(false) // Par défaut longue durée
                .build();
    }

    @Test
    public void should_calculate_correct_fiscal_arbitrage_for_classique() {
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
        assertEquals(0, BigDecimal.valueOf(800.00).compareTo(dto.resultatReelImposable())); // 3000 - 1200 - 1000 = 800

        // Micro-BIC: 50% abattement car meublé classique. Case 5NI
        assertEquals(0, BigDecimal.valueOf(1500.00).compareTo(dto.abattementMicroBic()));
        assertEquals(0, BigDecimal.valueOf(1500.00).compareTo(dto.resultatMicroBicImposable()));
        assertEquals("5NI", dto.caseFiscaleMicroBic());

        // Arbitrage: Réel (800) < Micro-BIC (1500) -> REGIME_REEL_AVANTAGEUX
        assertEquals("REGIME_REEL_AVANTAGEUX", dto.regimeFiscalConseille());
        assertEquals(0, BigDecimal.valueOf(700.00).compareTo(dto.differenceGainImposable()));
    }

    @Test
    public void should_calculate_correct_fiscal_arbitrage_for_meuble_tourisme_classe() {
        // Arrange
        logement.setQualificationLogement(QualificationLogement.MEUBLE_DE_TOURISME);
        logement.setEstMeubleTourisme(true);
        logement.setEstLoueCourteDuree(true);
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
        assertEquals(0, BigDecimal.valueOf(3900.00).compareTo(dto.resultatReelImposable()));

        // Micro-BIC: 50% abattement pour un meublé classé de tourisme sous Loi Le Meur 2026. Case 5NG
        assertEquals(0, BigDecimal.valueOf(2000.00).compareTo(dto.abattementMicroBic()));
        assertEquals(0, BigDecimal.valueOf(2000.00).compareTo(dto.resultatMicroBicImposable()));
        assertEquals("5NG", dto.caseFiscaleMicroBic());

        // Arbitrage: Micro-BIC (2000) < Réel (3900) -> MICRO_BIC_AVANTAGEUX
        assertEquals("MICRO_BIC_AVANTAGEUX", dto.regimeFiscalConseille());
        assertEquals(0, BigDecimal.valueOf(1900.00).compareTo(dto.differenceGainImposable()));
    }

    @Test
    public void should_calculate_correct_fiscal_arbitrage_for_meuble_tourisme_non_classe() {
        // Arrange
        logement.setEstLoueCourteDuree(true); // Courte durée mais non classé
        logement.setEstMeubleTourisme(false);
        when(logementService.getLogementById(10L, user)).thenReturn(logement);

        // Recettes: 10000€
        Recette r1 = Recette.builder().montantBrut(BigDecimal.valueOf(10000.00)).build();
        when(recetteRepository.findByLogementIdAndDateEncaissementBetween(eq(10L), any(), any()))
                .thenReturn(Collections.singletonList(r1));

        // Dépenses déductibles: 2000€
        Depense d1 = Depense.builder()
                .montantRetenu(BigDecimal.valueOf(2000.00))
                .statutDeductibilite(StatutDeductibilite.DEDUCTIBLE)
                .build();
        when(depenseRepository.findByLogementIdAndDateDepenseBetween(eq(10L), any(), any()))
                .thenReturn(Collections.singletonList(d1));

        when(bienAmortissableRepository.findByLogementId(10L)).thenReturn(Collections.emptyList());

        // Act
        BilanFiscalDto dto = service.calculerBilanFiscal(10L, 2026, user);

        // Assert
        assertNotNull(dto);
        assertEquals(0, BigDecimal.valueOf(8000.00).compareTo(dto.resultatReelImposable())); // 10000 - 2000

        // Micro-BIC: 30% abattement pour un meublé non classé sous Loi Le Meur 2026. Case 5NH
        assertEquals(0, BigDecimal.valueOf(3000.00).compareTo(dto.abattementMicroBic()));
        assertEquals(0, BigDecimal.valueOf(7000.00).compareTo(dto.resultatMicroBicImposable())); // 10000 - 3000
        assertEquals("5NH", dto.caseFiscaleMicroBic());

        // Arbitrage: Micro-BIC (7000) < Réel (8000) -> MICRO_BIC_AVANTAGEUX
        assertEquals("MICRO_BIC_AVANTAGEUX", dto.regimeFiscalConseille());
    }

    @Test
    public void should_force_real_regime_when_revenue_exceeds_threshold() {
        // Arrange
        logement.setEstLoueCourteDuree(true); // Courte durée non classé -> seuil 15000€
        logement.setEstMeubleTourisme(false);
        when(logementService.getLogementById(10L, user)).thenReturn(logement);

        // Recettes: 20000€ (dépasse le seuil de 15000€)
        Recette r1 = Recette.builder().montantBrut(BigDecimal.valueOf(20000.00)).build();
        when(recetteRepository.findByLogementIdAndDateEncaissementBetween(eq(10L), any(), any()))
                .thenReturn(Collections.singletonList(r1));

        // Dépenses déductibles: 1000€
        Depense d1 = Depense.builder()
                .montantRetenu(BigDecimal.valueOf(1000.00))
                .statutDeductibilite(StatutDeductibilite.DEDUCTIBLE)
                .build();
        when(depenseRepository.findByLogementIdAndDateDepenseBetween(eq(10L), any(), any()))
                .thenReturn(Collections.singletonList(d1));

        when(bienAmortissableRepository.findByLogementId(10L)).thenReturn(Collections.emptyList());

        // Act
        BilanFiscalDto dto = service.calculerBilanFiscal(10L, 2026, user);

        // Assert
        assertNotNull(dto);
        assertEquals("REGIME_REEL_AVANTAGEUX", dto.regimeFiscalConseille()); // Forcé
        assertEquals(0, BigDecimal.ZERO.compareTo(dto.differenceGainImposable()));
    }
}
