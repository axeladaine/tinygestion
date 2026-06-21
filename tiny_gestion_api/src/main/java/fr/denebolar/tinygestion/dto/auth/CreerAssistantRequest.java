package fr.denebolar.tinygestion.dto.auth;

public record CreerAssistantRequest(
    String email,
    String motDePasse,
    String prenom,
    String nom
) {}
