import { Logement } from './logement.model';
import { DocumentJustificatif } from './document-justificatif.model';

export interface Recette {
  id?: number;
  logement: Partial<Logement>;
  plateforme: 'AIRBNB' | 'BOOKING' | 'DIRECT' | 'AUTRE';
  nomClient?: string;
  dateDebutSejour?: string;
  dateFinSejour?: string;
  dateEncaissement?: string;
  nombreNuits?: number;
  montantBrut: number;
  fraisPlateforme?: number;
  montantTaxeSejour?: number;
  montantNetRecu?: number;
  commentaire?: string;
  documentJustificatifId?: number;
  documentJustificatif?: DocumentJustificatif;
  dateCreation?: string;
  dateModification?: string;
}
