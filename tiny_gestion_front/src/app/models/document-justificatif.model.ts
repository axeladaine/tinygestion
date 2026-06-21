import { Logement } from './logement.model';

export interface DocumentJustificatif {
  id?: number;
  logement: Partial<Logement>;
  nomFichier: string;
  typeFichier?: string;
  cheminStockage: string;
  typeDocument: 'FACTURE' | 'RELEVE_PLATEFORME' | 'DOCUMENT_FISCAL' | 'DOCUMENT_MAIRIE' | 'ASSURANCE' | 'IMPOT_LOCAL' | 'AUTRE';
  entiteLieeType?: string;
  entiteLieeId?: number;
  dateTeleversement?: string;
}
