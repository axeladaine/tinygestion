export interface BilanFiscal {
  logementId: number;
  annee: number;
  recettesBrutes: number;
  depensesRetenues: number;
  amortissementDisponible: number;
  resultatAvantAmortissement: number;
  amortissementUtilise: number;
  resultatReelImposable: number;
  amortissementNonUtilise: number;
  estMeubleTourisme: boolean;
  abattementMicroBic: number;
  resultatMicroBicImposable: number;
  regimeFiscalConseille: string; // 'REGIME_REEL_AVANTAGEUX' | 'MICRO_BIC_AVANTAGEUX' | 'EQUIVALENT'
  differenceGainImposable: number;
}
