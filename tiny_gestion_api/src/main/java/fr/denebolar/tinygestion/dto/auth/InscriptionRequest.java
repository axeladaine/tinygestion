package fr.denebolar.tinygestion.dto.auth;

public record InscriptionRequest(
    String email,
    String motDePasse,
    String prenom,
    String nom,
    String nomLogement,
    String adresseLogement,
    String codePostalLogement,
    String villeLogement
) {}
