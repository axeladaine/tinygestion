export interface JournalSecurite {
  id?: number;
  utilisateur?: {
    id: number;
    email: string;
    nom: string;
    prenom: string;
  };
  email: string;
  typeEvenement: string;
  adresseIp?: string;
  dateEvenement: string;
  details?: string;
}
