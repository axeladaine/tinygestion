import { Logement } from './logement.model';

export interface TacheChecklist {
  id: number;
  libelle: string;
  termine: boolean;
}

export interface ChecklistMensuelle {
  id?: number;
  logement?: Logement | { id: number };
  annee: number;
  mois: number;
  tachesJson: string;
  tauxCompletion?: number;
  dateCreation?: string;
  dateModification?: string;
}
