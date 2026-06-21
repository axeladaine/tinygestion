package fr.denebolar.tinygestion.service;

import fr.denebolar.tinygestion.domain.*;
import fr.denebolar.tinygestion.repository.LogementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LogementServiceTest {

    @Mock
    private LogementRepository logementRepository;

    @InjectMocks
    private LogementService service;

    private Utilisateur user;
    private Proprietaire proprietaire;
    private Logement logement;

    @BeforeEach
    public void setUp() {
        proprietaire = Proprietaire.builder().id(5L).nom("Dupont").build();
        user = Utilisateur.builder().id(1L).email("test@example.com").role(Role.PROPRIETAIRE).proprietaire(proprietaire).build();
        logement = Logement.builder().id(10L).nom("Tiny Cabane").proprietaire(proprietaire).build();
    }

    @Test
    public void should_return_logements_for_proprietaire() {
        when(logementRepository.findByProprietaireId(5L)).thenReturn(Collections.singletonList(logement));

        List<Logement> result = service.getLogements(user);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
    }

    @Test
    public void should_return_logement_by_id_when_authorized() {
        when(logementRepository.findById(10L)).thenReturn(Optional.of(logement));

        Logement result = service.getLogementById(10L, user);

        assertNotNull(result);
        assertEquals("Tiny Cabane", result.getNom());
    }

    @Test
    public void should_throw_exception_when_logement_not_found() {
        when(logementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            service.getLogementById(99L, user);
        });
    }

    @Test
    public void should_throw_exception_when_unauthorized_user() {
        Proprietaire anotherProprio = Proprietaire.builder().id(8L).build();
        Logement otherLogement = Logement.builder().id(20L).proprietaire(anotherProprio).build();

        when(logementRepository.findById(20L)).thenReturn(Optional.of(otherLogement));

        assertThrows(RuntimeException.class, () -> {
            service.getLogementById(20L, user);
        });
    }

    @Test
    public void should_save_new_logement() {
        when(logementRepository.save(any(Logement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Logement result = service.saveLogement(logement, user);

        assertNotNull(result);
        assertEquals(proprietaire, result.getProprietaire());
        verify(logementRepository, times(1)).save(logement);
    }

    @Test
    public void should_throw_exception_when_saving_without_proprietaire() {
        Utilisateur userWithoutProprio = Utilisateur.builder().id(1L).email("test@example.com").role(Role.ASSISTANT).build();

        assertThrows(RuntimeException.class, () -> {
            service.saveLogement(logement, userWithoutProprio);
        });
    }

    @Test
    public void should_update_existing_logement() {
        Logement details = Logement.builder()
                .nom("Tiny du Lac Rénovée")
                .adresse("20 Rue du lac")
                .codePostal("40150")
                .ville("Hossegor")
                .qualificationLogement(QualificationLogement.MEUBLE_DE_TOURISME)
                .estDeplacable(true)
                .estMeubleTourisme(true)
                .build();

        when(logementRepository.findById(10L)).thenReturn(Optional.of(logement));
        when(logementRepository.save(any(Logement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Logement result = service.updateLogement(10L, details, user);

        assertNotNull(result);
        assertEquals("Tiny du Lac Rénovée", result.getNom());
        assertEquals("20 Rue du lac", result.getAdresse());
        assertEquals("40150", result.getCodePostal());
        assertEquals("Hossegor", result.getVille());
        assertEquals(QualificationLogement.MEUBLE_DE_TOURISME, result.getQualificationLogement());
        assertTrue(result.isEstDeplacable());
        assertTrue(result.isEstMeubleTourisme());
        verify(logementRepository, times(1)).save(logement);
    }
}
