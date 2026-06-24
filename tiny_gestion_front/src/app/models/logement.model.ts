export interface Logement {
  id?: number;
  nom: string;
  adresse?: string;
  codePostal?: string;
  ville?: string;
  qualificationLogement: 'RESIDENCE_SECONDAIRE' | 'MEUBLE_DE_TOURISME' | 'DEPENDANCE' | 'A_VERIFIER';
  estDeplacable: boolean;
  estSurTerrainResidencePrincipale: boolean;
  estLoueCourteDuree: boolean;
  estMeubleTourisme: boolean;
  initialise: boolean;
  dateDebutLocation?: string;
  dateCreation?: string;
  dateModification?: string;
}
