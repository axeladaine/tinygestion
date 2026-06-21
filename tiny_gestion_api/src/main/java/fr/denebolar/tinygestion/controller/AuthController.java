package fr.denebolar.tinygestion.controller;

import fr.denebolar.tinygestion.dto.auth.AuthRequest;
import fr.denebolar.tinygestion.dto.auth.InscriptionRequest;
import fr.denebolar.tinygestion.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/authentification")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/connexion")
    public ResponseEntity<?> connexion(
            @RequestBody AuthRequest request,
            HttpServletRequest servletRequest
    ) {
        String ipAddress = servletRequest.getRemoteAddr();
        log.info("Requête de connexion reçue sur POST /api/authentification/connexion pour : {} (IP: {})", request.email(), ipAddress);
        try {
            return ResponseEntity.ok(authService.login(request, ipAddress));
        } catch (AuthenticationException e) {
            log.warn("Identifiants refusés par le service d'authentification pour {}", request.email());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou mot de passe incorrect");
        }
    }

    @PostMapping("/inscription")
    public ResponseEntity<?> inscription(
            @RequestBody InscriptionRequest request,
            HttpServletRequest servletRequest
    ) {
        String ipAddress = servletRequest.getRemoteAddr();
        log.info("Requête d'inscription reçue sur POST /api/authentification/inscription pour : {} (IP: {})", request.email(), ipAddress);
        try {
            return ResponseEntity.ok(authService.register(request, ipAddress));
        } catch (Exception e) {
            log.error("Erreur lors de l'inscription pour {} : {}", request.email(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
