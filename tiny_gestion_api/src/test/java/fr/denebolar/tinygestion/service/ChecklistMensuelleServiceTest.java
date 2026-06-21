package fr.denebolar.tinygestion.service;

import fr.denebolar.tinygestion.domain.*;
import fr.denebolar.tinygestion.repository.ChecklistMensuelleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChecklistMensuelleServiceTest {

    @Mock
    private ChecklistMensuelleRepository repository;

    @Mock
    private LogementService logementService;

    @InjectMocks
    private ChecklistMensuelleService service;

    private Utilisateur user;
    private Logement logement;

    @BeforeEach
    public void setUp() {
        user = Utilisateur.builder().id(1L).email("test@example.com").role(Role.PROPRIETAIRE).build();
        logement = Logement.builder().id(10L).nom("Tiny test").build();
    }

    @Test
    public void should_return_existing_checklist() {
        ChecklistMensuelle existing = ChecklistMensuelle.builder()
                .id(100L)
                .logement(logement)
                .annee(2026)
                .mois(6)
                .tachesJson("[]")
                .tauxCompletion(BigDecimal.ZERO)
                .build();

        when(logementService.getLogementById(10L, user)).thenReturn(logement);
        when(repository.findByLogementIdAndAnneeAndMois(10L, 2026, 6)).thenReturn(Optional.of(existing));

        ChecklistMensuelle result = service.getOrCreateChecklist(10L, 2026, 6, user);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        verify(repository, never()).save(any());
    }

    @Test
    public void should_create_default_checklist_when_not_exist() {
        when(logementService.getLogementById(10L, user)).thenReturn(logement);
        when(repository.findByLogementIdAndAnneeAndMois(10L, 2026, 6)).thenReturn(Optional.empty());
        when(repository.save(any(ChecklistMensuelle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChecklistMensuelle result = service.getOrCreateChecklist(10L, 2026, 6, user);

        assertNotNull(result);
        assertEquals(2026, result.getAnnee());
        assertEquals(6, result.getMois());
        assertTrue(result.getTachesJson().contains("\"termine\":false"));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getTauxCompletion()));
        verify(repository, times(1)).save(any());
    }

    @Test
    public void should_update_checklist_and_calculate_completion_rate() {
        ChecklistMensuelle checklist = ChecklistMensuelle.builder()
                .id(100L)
                .logement(logement)
                .annee(2026)
                .mois(6)
                .tachesJson("[]")
                .tauxCompletion(BigDecimal.ZERO)
                .build();

        String updatedJson = "[" +
                "{\"id\":1,\"termine\":true}," +
                "{\"id\":2,\"termine\":false}" +
                "]";

        when(repository.findById(100L)).thenReturn(Optional.of(checklist));
        when(logementService.getLogementById(10L, user)).thenReturn(logement);
        when(repository.save(any(ChecklistMensuelle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChecklistMensuelle result = service.updateChecklist(100L, updatedJson, user);

        assertNotNull(result);
        assertEquals(updatedJson, result.getTachesJson());
        // 1 true, 1 false -> 50%
        assertEquals(0, BigDecimal.valueOf(50).compareTo(result.getTauxCompletion()));
        verify(repository, times(1)).save(checklist);
    }

    @Test
    public void should_throw_exception_when_updateChecklist_not_found() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            service.updateChecklist(99L, "[]", user);
        });
    }

    @Test
    public void should_throw_exception_when_updateChecklist_unauthorized() {
        ChecklistMensuelle checklist = ChecklistMensuelle.builder()
                .id(100L)
                .logement(logement)
                .build();

        when(repository.findById(100L)).thenReturn(Optional.of(checklist));
        when(logementService.getLogementById(10L, user)).thenThrow(new RuntimeException("Unauthorized"));

        assertThrows(RuntimeException.class, () -> {
            service.updateChecklist(100L, "[]", user);
        });
    }

    @Test
    public void should_return_checklists_by_logement_and_annee() {
        when(logementService.getLogementById(10L, user)).thenReturn(logement);
        when(repository.findByLogementIdAndAnnee(10L, 2026)).thenReturn(Collections.singletonList(new ChecklistMensuelle()));

        List<ChecklistMensuelle> result = service.getChecklistsByLogementAndAnnee(10L, 2026, user);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void should_handle_empty_or_null_json_for_completion_rate() {
        ChecklistMensuelle checklist = ChecklistMensuelle.builder()
                .id(100L)
                .logement(logement)
                .build();

        when(repository.findById(100L)).thenReturn(Optional.of(checklist));
        when(logementService.getLogementById(10L, user)).thenReturn(logement);
        when(repository.save(any(ChecklistMensuelle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Test null JSON
        ChecklistMensuelle resultNull = service.updateChecklist(100L, null, user);
        assertEquals(BigDecimal.ZERO, resultNull.getTauxCompletion());

        // Test empty JSON
        ChecklistMensuelle resultEmpty = service.updateChecklist(100L, "", user);
        assertEquals(BigDecimal.ZERO, resultEmpty.getTauxCompletion());

        // Test JSON with no tasks
        ChecklistMensuelle resultNoTasks = service.updateChecklist(100L, "[]", user);
        assertEquals(BigDecimal.ZERO, resultNoTasks.getTauxCompletion());
    }
}
