import { Logement } from './logement.model';
import { DocumentJustificatif } from './document-justificatif.model';

export interface Depense {
  id?: number;
  logement: Partial<Logement>;
  dateDepense: string;
  fournisseur?: string;
  categorie?: string;
  montantTtc: number;
  tauxAffectationLocation: number;
  montantRetenu?: number;
  moyenPaiement?: string;
  statutDeductibilite: 'DEDUCTIBLE' | 'NON_DEDUCTIBLE' | 'A_VERIFIER';
  commentaire?: string;
  documentJustificatifId?: number;
  documentJustificatif?: DocumentJustificatif;
  dateCreation?: string;
  dateModification?: string;
}
