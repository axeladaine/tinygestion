package fr.denebolar.tinygestion.dto.logement;

import java.math.BigDecimal;

public record InitialisationLogementDto(
        BigDecimal recettesAnterieures,
        BigDecimal depensesAnterieures
) {}
