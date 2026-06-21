package fr.denebolar.tinygestion.dto.dashboard;

import java.math.BigDecimal;

public record StatsMensuellesDto(
    BigDecimal totalRecettes,
    BigDecimal totalDepenses,
    BigDecimal resultatEstime,
    long justificatifsManquants
) {}
