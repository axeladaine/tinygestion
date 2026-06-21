package fr.denebolar.tinygestion.dto.auth;

public record AuthResponse(String token, UtilisateurDto utilisateur) {}
