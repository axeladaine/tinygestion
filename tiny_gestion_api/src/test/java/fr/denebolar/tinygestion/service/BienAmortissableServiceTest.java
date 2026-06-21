package fr.denebolar.tinygestion.service;

import fr.denebolar.tinygestion.domain.*;
import fr.denebolar.tinygestion.repository.BienAmortissableRepository;
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
public class BienAmortissableServiceTest {

    @Mock
    private BienAmortissableRepository repository;

    @Mock
    private LogementService logementService;

    @InjectMocks
    private BienAmortissableService service;

    private Utilisateur user;
    private Logement logement;
    private BienAmortissable bien;

    @BeforeEach
    public void setUp() {
        user = Utilisateur.builder().id(1L).email("test@example.com").role(Role.PROPRIETAIRE).build();
        logement = Logement.builder().id(10L).nom("Tiny test").build();
        bien = BienAmortissable.builder()
                .id(300L)
                .nom("Terrasse en Pin")
                .categorie(CategorieBien.TERRASSE)
                .dateAchat(LocalDate.now())
                .montantTtc(BigDecimal.valueOf(3200.00))
                .tauxAffectationLocation(BigDecimal.valueOf(100.00))
                .dureeAmortissementAns(10)
                .logement(logement)
                .build();
    }

    @Test
    public void should_calculate_amortization_correctly_on_model() {
        bien.calculateAmortization();
        assertEquals(0, BigDecimal.valueOf(3200.00).compareTo(bien.getBaseAmortissable()));
        assertEquals(0, BigDecimal.valueOf(320.00).compareTo(bien.getAmortissementAnnuel()));
        assertEquals(0, BigDecimal.valueOf(26.67).compareTo(bien.getAmortissementMensuel()));
    }

    @Test
    public void should_calculate_amortization_with_partial_allocation_on_model() {
        BienAmortissable bienPartiel = BienAmortissable.builder()
                .nom("Chauffage Tiny")
                .categorie(CategorieBien.CHAUFFAGE_CLIMATISATION)
                .dateAchat(LocalDate.now())
                .montantTtc(BigDecimal.valueOf(1500.00))
                .tauxAffectationLocation(BigDecimal.valueOf(40.00))
                .dureeAmortissementAns(8)
                .build();

        bienPartiel.calculateAmortization();
        assertEquals(0, BigDecimal.valueOf(600.00).compareTo(bienPartiel.getBaseAmortissable()));
        assertEquals(0, BigDecimal.valueOf(75.00).compareTo(bienPartiel.getAmortissementAnnuel()));
        assertEquals(0, BigDecimal.valueOf(6.25).compareTo(bienPartiel.getAmortissementMensuel()));
    }

    @Test
    public void should_return_biens_for_logement() {
        when(logementService.getLogementById(10L, user)).thenReturn(logement);
        when(repository.findByLogementId(10L)).thenReturn(Collections.singletonList(bien));

        List<BienAmortissable> result = service.getBiensByLogement(10L, user);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(300L, result.get(0).getId());
    }

    @Test
    public void should_save_new_bien() {
        when(logementService.getLogementById(10L, user)).thenReturn(logement);
        when(repository.save(any(BienAmortissable.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BienAmortissable result = service.saveBien(bien, user);

        assertNotNull(result);
        assertEquals(logement, result.getLogement());
        verify(repository, times(1)).save(bien);
    }

    @Test
    public void should_update_existing_bien() {
        BienAmortissable details = BienAmortissable.builder()
                .nom("Climatisation réactive")
                .categorie(CategorieBien.CHAUFFAGE_CLIMATISATION)
                .montantTtc(BigDecimal.valueOf(2500))
                .tauxAffectationLocation(BigDecimal.valueOf(90))
                .dureeAmortissementAns(5)
                .build();

        when(repository.findById(300L)).thenReturn(Optional.of(bien));
        when(logementService.getLogementById(10L, user)).thenReturn(logement);
        when(repository.save(any(BienAmortissable.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BienAmortissable result = service.updateBien(300L, details, user);

        assertNotNull(result);
        assertEquals("Climatisation réactive", result.getNom());
        assertEquals(CategorieBien.CHAUFFAGE_CLIMATISATION, result.getCategorie());
        assertEquals(0, BigDecimal.valueOf(2500).compareTo(result.getMontantTtc()));
        assertEquals(0, BigDecimal.valueOf(90).compareTo(result.getTauxAffectationLocation()));
        assertEquals(5, result.getDureeAmortissementAns());
        verify(repository, times(1)).save(bien);
    }

    @Test
    public void should_delete_existing_bien() {
        when(repository.findById(300L)).thenReturn(Optional.of(bien));
        when(logementService.getLogementById(10L, user)).thenReturn(logement);

        service.deleteBien(300L, user);

        verify(repository, times(1)).delete(bien);
    }

    @Test
    public void should_update_bien_and_change_logement() {
        Logement otherLogement = Logement.builder().id(20L).nom("Autre Tiny").build();
        BienAmortissable details = BienAmortissable.builder()
                .nom("Climatisation réactive")
                .logement(otherLogement)
                .build();

        when(repository.findById(300L)).thenReturn(Optional.of(bien));
        when(logementService.getLogementById(10L, user)).thenReturn(logement);
        when(logementService.getLogementById(20L, user)).thenReturn(otherLogement);
        when(repository.save(any(BienAmortissable.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BienAmortissable result = service.updateBien(300L, details, user);

        assertNotNull(result);
        assertEquals(otherLogement, result.getLogement());
    }

    @Test
    public void should_throw_exception_when_updating_non_existent_bien() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            service.updateBien(99L, bien, user);
        });
    }

    @Test
    public void should_throw_exception_when_deleting_non_existent_bien() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            service.deleteBien(99L, user);
        });
    }

    @Test
    public void should_handle_model_edge_cases() {
        // Test base calculations on edge cases
        BienAmortissable edgeBien = BienAmortissable.builder()
                .montantTtc(BigDecimal.valueOf(100))
                .tauxAffectationLocation(null) // should default to 100
                .dureeAmortissementAns(0) // should set annual/monthly to ZERO
                .build();

        edgeBien.calculateAmortization();
        assertEquals(0, BigDecimal.valueOf(100).compareTo(edgeBien.getBaseAmortissable()));
        assertEquals(BigDecimal.ZERO, edgeBien.getAmortissementAnnuel());
        assertEquals(BigDecimal.ZERO, edgeBien.getAmortissementMensuel());
    }
}
