package fr.denebolar.tinygestion.service;

import fr.denebolar.tinygestion.domain.*;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecetteServiceTest {

    @Mock
    private RecetteRepository repository;

    @Mock
    private LogementService logementService;

    @InjectMocks
    private RecetteService service;

    private Utilisateur user;
    private Logement logement;
    private Recette recette;

    @BeforeEach
    public void setUp() {
        user = Utilisateur.builder().id(1L).email("test@example.com").role(Role.PROPRIETAIRE).build();
        logement = Logement.builder().id(10L).nom("Tiny test").build();
        recette = Recette.builder().id(100L).logement(logement).montantBrut(BigDecimal.valueOf(250)).build();
    }

    @Test
    public void should_return_recettes_for_logement() {
        when(logementService.getLogementById(10L, user)).thenReturn(logement);
        when(repository.findByLogementId(10L)).thenReturn(Collections.singletonList(recette));

        List<Recette> result = service.getRecettesByLogement(10L, user);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getId());
    }

    @Test
    public void should_return_recettes_for_logement_and_period() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 12, 31);
        when(logementService.getLogementById(10L, user)).thenReturn(logement);
        when(repository.findByLogementIdAndDateEncaissementBetween(10L, start, end))
                .thenReturn(Collections.singletonList(recette));

        List<Recette> result = service.getRecettesByLogementAndPeriod(10L, start, end, user);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository, times(1)).findByLogementIdAndDateEncaissementBetween(10L, start, end);
    }

    @Test
    public void should_save_new_recette() {
        when(logementService.getLogementById(10L, user)).thenReturn(logement);
        when(repository.save(any(Recette.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Recette result = service.saveRecette(recette, user);

        assertNotNull(result);
        assertEquals(logement, result.getLogement());
        verify(repository, times(1)).save(recette);
    }

    @Test
    public void should_update_existing_recette() {
        Recette details = Recette.builder()
                .plateforme(Plateforme.BOOKING)
                .nomClient("M. Martin")
                .montantBrut(BigDecimal.valueOf(400))
                .build();

        when(repository.findById(100L)).thenReturn(Optional.of(recette));
        when(logementService.getLogementById(10L, user)).thenReturn(logement);
        when(repository.save(any(Recette.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Recette result = service.updateRecette(100L, details, user);

        assertNotNull(result);
        assertEquals(Plateforme.BOOKING, result.getPlateforme());
        assertEquals("M. Martin", result.getNomClient());
        assertEquals(0, BigDecimal.valueOf(400).compareTo(result.getMontantBrut()));
        verify(repository, times(1)).save(recette);
    }

    @Test
    public void should_update_existing_recette_with_different_logement() {
        Logement otherLogement = Logement.builder().id(20L).nom("Autre Tiny").build();
        Recette details = Recette.builder()
                .logement(otherLogement)
                .plateforme(Plateforme.BOOKING)
                .build();

        when(repository.findById(100L)).thenReturn(Optional.of(recette));
        when(logementService.getLogementById(10L, user)).thenReturn(logement);
        when(logementService.getLogementById(20L, user)).thenReturn(otherLogement);
        when(repository.save(any(Recette.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Recette result = service.updateRecette(100L, details, user);

        assertNotNull(result);
        assertEquals(otherLogement, result.getLogement());
        verify(repository, times(1)).save(recette);
    }

    @Test
    public void should_delete_existing_recette() {
        when(repository.findById(100L)).thenReturn(Optional.of(recette));
        when(logementService.getLogementById(10L, user)).thenReturn(logement);

        service.deleteRecette(100L, user);

        verify(repository, times(1)).delete(recette);
    }
}
