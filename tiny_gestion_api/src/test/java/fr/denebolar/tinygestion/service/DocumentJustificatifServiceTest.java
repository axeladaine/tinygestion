package fr.denebolar.tinygestion.service;

import fr.denebolar.tinygestion.domain.*;
import fr.denebolar.tinygestion.repository.DocumentJustificatifRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentJustificatifServiceTest {

    @Mock
    private DocumentJustificatifRepository documentRepository;

    @Mock
    private LogementService logementService;

    @InjectMocks
    private DocumentJustificatifService service;

    private Utilisateur user;
    private Logement logement;
    private DocumentJustificatif document;
    private Path rootLocation = Paths.get("uploads/documents");

    @BeforeEach
    public void setUp() {
        user = Utilisateur.builder().id(1L).email("user@example.com").build();
        logement = Logement.builder().id(10L).nom("Tiny Cabane").build();
        document = DocumentJustificatif.builder()
                .id(100L)
                .logement(logement)
                .nomFichier("test.pdf")
                .typeFichier("application/pdf")
                .cheminStockage("mock-uuid_test.pdf")
                .typeDocument(TypeDocument.FACTURE)
                .build();
    }

    @Test
    public void should_store_document_successfully_when_valid() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "fichier",
                "test.pdf",
                "application/pdf",
                "pdf content".getBytes()
        );

        when(logementService.getLogementById(10L, user)).thenReturn(logement);
        when(documentRepository.save(any(DocumentJustificatif.class))).thenAnswer(invocation -> {
            DocumentJustificatif saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        DocumentJustificatif result = service.stockerDocument(
                file, 10L, TypeDocument.FACTURE, "DEPENSE", 5L, user
        );

        assertNotNull(result);
        assertEquals("test.pdf", result.getNomFichier());
        assertEquals("application/pdf", result.getTypeFichier());
        assertEquals(TypeDocument.FACTURE, result.getTypeDocument());
        assertEquals("DEPENSE", result.getEntiteLieeType());
        assertEquals(5L, result.getEntiteLieeId());

        // Nettoyer le fichier créé sur le disque lors du test
        Path createdFile = rootLocation.resolve(result.getCheminStockage());
        Files.deleteIfExists(createdFile);
    }

    @Test
    public void should_throw_exception_when_file_is_empty() {
        MockMultipartFile file = new MockMultipartFile("fichier", "test.pdf", "application/pdf", new byte[0]);
        when(logementService.getLogementById(10L, user)).thenReturn(logement);

        assertThrows(RuntimeException.class, () -> {
            service.stockerDocument(file, 10L, TypeDocument.FACTURE, null, null, user);
        });
    }

    @Test
    public void should_throw_exception_when_file_has_invalid_extension() {
        MockMultipartFile file = new MockMultipartFile("fichier", "test.txt", "text/plain", "content".getBytes());
        when(logementService.getLogementById(10L, user)).thenReturn(logement);

        assertThrows(RuntimeException.class, () -> {
            service.stockerDocument(file, 10L, TypeDocument.FACTURE, null, null, user);
        });
    }

    @Test
    public void should_return_documents_by_logement() {
        when(logementService.getLogementById(10L, user)).thenReturn(logement);
        when(documentRepository.findByLogementId(10L)).thenReturn(Collections.singletonList(document));

        List<DocumentJustificatif> result = service.getDocumentsByLogement(10L, user);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test.pdf", result.get(0).getNomFichier());
    }

    @Test
    public void should_return_document_by_id() {
        when(documentRepository.findById(100L)).thenReturn(Optional.of(document));
        when(logementService.getLogementById(logement.getId(), user)).thenReturn(logement);

        DocumentJustificatif result = service.getDocumentById(100L, user);

        assertNotNull(result);
        assertEquals(100L, result.getId());
    }

    @Test
    public void should_throw_exception_when_document_not_found() {
        when(documentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            service.getDocumentById(99L, user);
        });
    }

    @Test
    public void should_load_resource_when_exists() throws IOException {
        // Préparer un fichier physique pour le test
        if (!Files.exists(rootLocation)) {
            Files.createDirectories(rootLocation);
        }
        Path testFile = rootLocation.resolve("mock-uuid_test.pdf");
        Files.write(testFile, "pdf content".getBytes());

        when(documentRepository.findById(100L)).thenReturn(Optional.of(document));
        when(logementService.getLogementById(logement.getId(), user)).thenReturn(logement);

        Resource resource = service.chargerFichier(100L, user);

        assertNotNull(resource);
        assertTrue(resource.exists());

        // Nettoyage
        Files.deleteIfExists(testFile);
    }

    @Test
    public void should_throw_exception_when_file_not_found_on_disk() {
        when(documentRepository.findById(100L)).thenReturn(Optional.of(document));
        when(logementService.getLogementById(logement.getId(), user)).thenReturn(logement);

        assertThrows(RuntimeException.class, () -> {
            service.chargerFichier(100L, user);
        });
    }

    @Test
    public void should_delete_document_and_file() throws IOException {
        // Préparer un fichier physique pour le test
        if (!Files.exists(rootLocation)) {
            Files.createDirectories(rootLocation);
        }
        Path testFile = rootLocation.resolve("mock-uuid_test.pdf");
        Files.write(testFile, "pdf content".getBytes());

        when(documentRepository.findById(100L)).thenReturn(Optional.of(document));
        when(logementService.getLogementById(logement.getId(), user)).thenReturn(logement);

        service.supprimerDocument(100L, user);

        assertFalse(Files.exists(testFile));
        verify(documentRepository, times(1)).delete(document);
    }

    @Test
    public void should_throw_exception_when_path_traversal_detected() {
        MockMultipartFile file = new MockMultipartFile(
                "fichier",
                "subdir/test.pdf",
                "application/pdf",
                "pdf content".getBytes()
        );

        when(logementService.getLogementById(10L, user)).thenReturn(logement);

        assertThrows(RuntimeException.class, () -> {
            service.stockerDocument(file, 10L, TypeDocument.FACTURE, null, null, user);
        });
    }

    @Test
    public void should_continue_when_file_deletion_throws_exception() {
        DocumentJustificatif corruptDoc = DocumentJustificatif.builder()
                .id(200L)
                .logement(logement)
                .cheminStockage("") // pointer sur un dossier provoque une exception sur deleteIfExists (DirectoryNotEmptyException)
                .build();

        when(documentRepository.findById(200L)).thenReturn(Optional.of(corruptDoc));
        when(logementService.getLogementById(logement.getId(), user)).thenReturn(logement);

        // Devrait s'exécuter sans crash en catcheant l'erreur d'IO et en supprimant quand même l'entité
        assertDoesNotThrow(() -> {
            service.supprimerDocument(200L, user);
        });

        verify(documentRepository, times(1)).delete(corruptDoc);
    }
}
