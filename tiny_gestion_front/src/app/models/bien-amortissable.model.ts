import { Logement } from './logement.model';
import { DocumentJustificatif } from './document-justificatif.model';

export interface BienAmortissable {
  id?: number;
  logement: Partial<Logement>;
  nom: string;
  categorie: 'TINY_HOUSE' | 'TERRASSE' | 'RACCORDEMENT' | 'MOBILIER' | 'LITERIE' | 'ELECTROMENAGER' | 'CHAUFFAGE_CLIMATISATION' | 'DECORATION' | 'AUTRE';
  dateAchat: string;
  dateMiseEnService?: string;
  montantTtc: number;
  tauxAffectationLocation: number;
  baseAmortissable?: number;
  dureeAmortissementAns: number;
  amortissementAnnuel?: number;
  amortissementMensuel?: number;
  amortissementDeduitCumule?: number;
  amortissementNonUtiliseCumule?: number;
  possedeFacture?: boolean;
  documentJustificatifId?: number;
  documentJustificatif?: DocumentJustificatif;
  commentaire?: string;
  dateCreation?: string;
  dateModification?: string;
}
