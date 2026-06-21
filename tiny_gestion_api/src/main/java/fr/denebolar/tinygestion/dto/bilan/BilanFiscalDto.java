package fr.denebolar.tinygestion.dto.bilan;

import java.math.BigDecimal;

public record BilanFiscalDto(
        Long logementId,
        Integer annee,
        BigDecimal recettesBrutes,
        BigDecimal depensesRetenues,
        BigDecimal amortissementDisponible,
        BigDecimal resultatAvantAmortissement,
        BigDecimal amortissementUtilise,
        BigDecimal resultatReelImposable,
        BigDecimal amortissementNonUtilise,
        Boolean estMeubleTourisme,
        BigDecimal abattementMicroBic,
        BigDecimal resultatMicroBicImposable,
        String regimeFiscalConseille,
        BigDecimal differenceGainImposable
) {}
