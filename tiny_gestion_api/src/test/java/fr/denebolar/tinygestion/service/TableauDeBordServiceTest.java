package fr.denebolar.tinygestion.service;

import fr.denebolar.tinygestion.domain.*;
import fr.denebolar.tinygestion.dto.dashboard.StatsMensuellesDto;
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
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TableauDeBordServiceTest {

    @Mock
    private RecetteRepository recetteRepository;

    @Mock
    private DepenseRepository depenseRepository;

    @Mock
    private LogementService logementService;

    @InjectMocks
    private TableauDeBordService service;

    private Utilisateur user;
    private Logement logement;
    private Recette recetteAvecJustif;
    private Recette recetteSansJustif;
    private Depense depenseAvecJustif;
    private Depense depenseSansJustif;

    @BeforeEach
    public void setUp() {
        user = Utilisateur.builder().id(1L).email("user@example.com").build();
        logement = Logement.builder().id(10L).nom("Tiny").build();

        DocumentJustificatif justif = DocumentJustificatif.builder().id(100L).build();

        recetteAvecJustif = Recette.builder()
                .montantBrut(new BigDecimal("150.00"))
                .documentJustificatif(justif)
                .build();

        recetteSansJustif = Recette.builder()
                .montantBrut(new BigDecimal("100.00"))
                .documentJustificatif(null)
                .build();

        depenseAvecJustif = Depense.builder()
                .montantRetenu(new BigDecimal("40.00"))
                .documentJustificatif(justif)
                .build();

        depenseSansJustif = Depense.builder()
                .montantRetenu(new BigDecimal("30.00"))
                .documentJustificatif(null)
                .build();
    }

    @Test
    public void should_calculate_stats_mensuelles_successfully() {
        LocalDate start = LocalDate.of(2026, 6, 1);
        LocalDate end = LocalDate.of(2026, 6, 30);

        when(logementService.getLogementById(10L, user)).thenReturn(logement);
        when(recetteRepository.findByLogementIdAndDateEncaissementBetween(10L, start, end))
                .thenReturn(Arrays.asList(recetteAvecJustif, recetteSansJustif));
        when(depenseRepository.findByLogementIdAndDateDepenseBetween(10L, start, end))
                .thenReturn(Arrays.asList(depenseAvecJustif, depenseSansJustif));

        StatsMensuellesDto result = service.getStatsMensuelles(10L, 6, 2026, user);

        assertNotNull(result);
        assertEquals(new BigDecimal("250.00"), result.totalRecettes());
        assertEquals(new BigDecimal("70.00"), result.totalDepenses());
        assertEquals(new BigDecimal("180.00"), result.resultatEstime());
        assertEquals(2, result.justificatifsManquants());

        verify(logementService, times(1)).getLogementById(10L, user);
    }

    @Test
    public void should_handle_empty_values_and_null_amounts() {
        LocalDate start = LocalDate.of(2026, 6, 1);
        LocalDate end = LocalDate.of(2026, 6, 30);

        Recette nullAmountRecette = Recette.builder().montantBrut(null).documentJustificatif(null).build();
        Depense nullAmountDepense = Depense.builder().montantRetenu(null).documentJustificatif(null).build();

        when(logementService.getLogementById(10L, user)).thenReturn(logement);
        when(recetteRepository.findByLogementIdAndDateEncaissementBetween(10L, start, end))
                .thenReturn(Collections.singletonList(nullAmountRecette));
        when(depenseRepository.findByLogementIdAndDateDepenseBetween(10L, start, end))
                .thenReturn(Collections.singletonList(nullAmountDepense));

        StatsMensuellesDto result = service.getStatsMensuelles(10L, 6, 2026, user);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.totalRecettes());
        assertEquals(BigDecimal.ZERO, result.totalDepenses());
        assertEquals(BigDecimal.ZERO, result.resultatEstime());
        assertEquals(2, result.justificatifsManquants());
    }
}
