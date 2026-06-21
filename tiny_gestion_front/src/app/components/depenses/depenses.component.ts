import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FlatpickrDirective } from 'angularx-flatpickr';
import { ActivatedRoute, Router } from '@angular/router';
import { DepenseService } from '../../services/depense.service';
import { LogementService } from '../../services/logement.service';
import { DocumentJustificatifService } from '../../services/document-justificatif.service';
import { Depense } from '../../models/depense.model';
import { Logement } from '../../models/logement.model';
import { DocumentJustificatif } from '../../models/document-justificatif.model';

@Component({
  selector: 'app-depenses',
  standalone: true,
  imports: [CommonModule, FormsModule, FlatpickrDirective],
  templateUrl: './depenses.component.html',
  styleUrls: ['./depenses.component.css']
})
export class DepensesComponent implements OnInit {
  private depenseService = inject(DepenseService);
  private logementService = inject(LogementService);
  private docService = inject(DocumentJustificatifService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  logementActif: Logement | null = null;
  depenses: Depense[] = [];
  documents: DocumentJustificatif[] = [];
  chargement: boolean = true;
  afficherFormulaire: boolean = false;
  messageSucces: string | null = null;
  messageErreur: string | null = null;
  sauvegardeEnCours: boolean = false;

  // Variables pour la modification et l'upload en direct
  modeEdition: boolean = false;
  depenseIdEnCours: number | null = null;
  methodeJustificatif: 'lier' | 'televerser' = 'lier';
  fichierSelectionne: File | null = null;

  // Formulaire Dépense
  nouvelleDepense: Depense = this.getResetDepenseForm();

  categories = [
    'Électricité et eau',
    'Assurance',
    'Ménage et blanchisserie',
    'Consommables et accueil',
    'Linge de maison',
    'Entretien et petites réparations',
    'Frais de plateformes',
    'Impôts locaux (CFE, etc.)',
    'Abonnements (Application, internet)',
    'Honoraires (Comptabilité, conseil)',
    'Autre dépense'
  ];

  statutsDeductibilite = [
    { value: 'DEDUCTIBLE', label: 'Déductible' },
    { value: 'NON_DEDUCTIBLE', label: 'Non déductible' },
    { value: 'A_VERIFIER', label: 'À vérifier (par défaut)' }
  ];

  ngOnInit(): void {
    this.chargerLogements();
  }

  getResetDepenseForm(): Depense {
    return {
      logement: {},
      dateDepense: new Date().toISOString().split('T')[0],
      fournisseur: '',
      categorie: 'Électricité et eau',
      montantTtc: 0,
      tauxAffectationLocation: 100,
      moyenPaiement: 'Carte bancaire',
      statutDeductibilite: 'A_VERIFIER',
      commentaire: ''
    };
  }

  chargerLogements(): void {
    this.logementService.getLogements().subscribe({
      next: (logements) => {
        if (logements && logements.length > 0) {
          this.logementActif = logements[0];
          this.chargerDepenses();
          this.chargerDocuments();
        } else {
          this.chargement = false;
        }
      },
      error: () => {
        this.messageErreur = 'Erreur lors du chargement des logements.';
        this.chargement = false;
      }
    });
  }

  chargerDocuments(): void {
    if (!this.logementActif || !this.logementActif.id) return;
    this.docService.getDocuments(this.logementActif.id).subscribe({
      next: (docs) => {
        this.documents = docs.filter(d => d.typeDocument === 'FACTURE' || d.typeDocument === 'AUTRE');
      }
    });
  }

  telechargerJustificatif(doc: DocumentJustificatif): void {
    if (!doc.id) return;
    this.docService.telecharger(doc.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = doc.nomFichier;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      },
      error: () => {
        this.messageErreur = 'Erreur lors du téléchargement du justificatif.';
      }
    });
  }

  chargerDepenses(): void {
    if (!this.logementActif || !this.logementActif.id) return;

    this.chargement = true;
    this.depenseService.getDepenses(this.logementActif.id).subscribe({
      next: (data) => {
        this.depenses = data.sort((a, b) => {
          const dateA = a.dateDepense ? new Date(a.dateDepense).getTime() : 0;
          const dateB = b.dateDepense ? new Date(b.dateDepense).getTime() : 0;
          return dateB - dateA; // Décroissant
        });
        this.chargement = false;

        // Vérifier s'il y a un paramètre editDepenseId dans l'URL (redirection depuis le TBB)
        const editId = this.route.snapshot.queryParams['editDepenseId'];
        if (editId) {
          const depenseToEdit = this.depenses.find(d => d.id === +editId);
          if (depenseToEdit) {
            this.chargerFormulaireEdition(depenseToEdit);
            this.methodeJustificatif = 'televerser';

            // Nettoyer l'URL
            this.router.navigate([], {
              relativeTo: this.route,
              queryParams: { editDepenseId: null },
              queryParamsHandling: 'merge'
            });
          }
        }
      },
      error: () => {
        this.messageErreur = 'Erreur lors du chargement des dépenses.';
        this.chargement = false;
      }
    });
  }

  toggleFormulaire(): void {
    this.afficherFormulaire = !this.afficherFormulaire;
    if (!this.afficherFormulaire) {
      this.annulerEdition();
    }
  }

  chargerFormulaireEdition(depense: Depense): void {
    this.modeEdition = true;
    this.depenseIdEnCours = depense.id || null;
    this.nouvelleDepense = { ...depense };
    this.methodeJustificatif = 'lier';
    this.fichierSelectionne = null;
    this.afficherFormulaire = true;

    if (this.nouvelleDepense.dateDepense) {
      this.nouvelleDepense.dateDepense = this.nouvelleDepense.dateDepense.split('T')[0];
    }
  }

  annulerEdition(): void {
    this.modeEdition = false;
    this.depenseIdEnCours = null;
    this.nouvelleDepense = this.getResetDepenseForm();
    this.methodeJustificatif = 'lier';
    this.fichierSelectionne = null;
  }

  onFichierSelectionne(event: any): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.fichierSelectionne = input.files[0];
    }
  }

  // Calcul temps réel du montant retenu en frontend
  get montantRetenuCalcule(): number {
    const ttc = this.nouvelleDepense.montantTtc || 0;
    const taux = this.nouvelleDepense.tauxAffectationLocation || 100;
    return (ttc * taux) / 100;
  }

  onSubmit(): void {
    if (!this.logementActif || !this.logementActif.id) return;

    this.sauvegardeEnCours = true;
    this.messageSucces = null;
    this.messageErreur = null;

    this.nouvelleDepense.logement = { id: this.logementActif.id };
    this.nouvelleDepense.montantRetenu = this.montantRetenuCalcule;

    // Téléverser d'abord si sélectionné
    if (this.methodeJustificatif === 'televerser' && this.fichierSelectionne) {
      this.docService.televerser(this.fichierSelectionne, this.logementActif.id, 'FACTURE').subscribe({
        next: (doc) => {
          this.nouvelleDepense.documentJustificatif = doc;
          this.sauvegarderEntite();
        },
        error: () => {
          this.sauvegardeEnCours = false;
          this.messageErreur = "Erreur lors du téléversement du justificatif.";
        }
      });
    } else {
      this.sauvegarderEntite();
    }
  }

  private sauvegarderEntite(): void {
    if (this.modeEdition && this.depenseIdEnCours) {
      this.depenseService.updateDepense(this.depenseIdEnCours, this.nouvelleDepense).subscribe({
        next: () => {
          this.sauvegardeEnCours = false;
          this.messageSucces = 'Dépense modifiée avec succès !';
          this.afficherFormulaire = false;
          this.annulerEdition();
          this.chargerDepenses();
          this.chargerDocuments();
          setTimeout(() => this.messageSucces = null, 3000);
        },
        error: () => {
          this.sauvegardeEnCours = false;
          this.messageErreur = "Une erreur est survenue lors de la modification.";
        }
      });
    } else {
      this.depenseService.saveDepense(this.nouvelleDepense).subscribe({
        next: () => {
          this.sauvegardeEnCours = false;
          this.messageSucces = 'Dépense ajoutée avec succès !';
          this.afficherFormulaire = false;
          this.annulerEdition();
          this.chargerDepenses();
          this.chargerDocuments();
          setTimeout(() => this.messageSucces = null, 3000);
        },
        error: () => {
          this.sauvegardeEnCours = false;
          this.messageErreur = "Une erreur est survenue lors de l'enregistrement.";
        }
      });
    }
  }

  supprimer(id: number | undefined): void {
    if (!id || !confirm('Voulez-vous vraiment supprimer cette dépense ?')) return;

    this.depenseService.deleteDepense(id).subscribe({
      next: () => {
        this.messageSucces = 'Dépense supprimée avec succès.';
        this.chargerDepenses();
        setTimeout(() => this.messageSucces = null, 3000);
      },
      error: () => {
        this.messageErreur = 'Erreur lors de la suppression.';
      }
    });
  }
}
