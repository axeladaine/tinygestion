import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FlatpickrDirective } from 'angularx-flatpickr';
import { RecetteService } from '../../services/recette.service';
import { LogementService } from '../../services/logement.service';
import { DocumentJustificatifService } from '../../services/document-justificatif.service';
import { Recette } from '../../models/recette.model';
import { Logement } from '../../models/logement.model';
import { DocumentJustificatif } from '../../models/document-justificatif.model';

@Component({
  selector: 'app-recettes',
  standalone: true,
  imports: [CommonModule, FormsModule, FlatpickrDirective],
  templateUrl: './recettes.component.html',
  styleUrls: ['./recettes.component.css']
})
export class RecettesComponent implements OnInit {
  private recetteService = inject(RecetteService);
  private logementService = inject(LogementService);
  private docService = inject(DocumentJustificatifService);

  logementActif: Logement | null = null;
  recettes: Recette[] = [];
  documents: DocumentJustificatif[] = [];
  chargement: boolean = true;
  afficherFormulaire: boolean = false;
  messageSucces: string | null = null;
  messageErreur: string | null = null;
  sauvegardeEnCours: boolean = false;

  // Variables pour la modification et l'upload en direct
  modeEdition: boolean = false;
  recetteIdEnCours: number | null = null;
  methodeJustificatif: 'lier' | 'televerser' = 'lier';
  fichierSelectionne: File | null = null;

  // Formulaire Recette
  nouvelleRecette: Recette = this.getResetRecetteForm();

  plateformes = [
    { value: 'AIRBNB', label: 'Airbnb' },
    { value: 'BOOKING', label: 'Booking' },
    { value: 'DIRECT', label: 'En direct' },
    { value: 'AUTRE', label: 'Autre plateforme' }
  ];

  ngOnInit(): void {
    this.chargerLogements();
  }

  getResetRecetteForm(): Recette {
    return {
      logement: {},
      plateforme: 'AIRBNB',
      nomClient: '',
      dateDebutSejour: '',
      dateFinSejour: '',
      dateEncaissement: new Date().toISOString().split('T')[0],
      nombreNuits: 1,
      montantBrut: 0,
      fraisPlateforme: 0,
      montantTaxeSejour: 0,
      commentaire: ''
    };
  }

  chargerLogements(): void {
    this.logementService.getLogements().subscribe({
      next: (logements) => {
        if (logements && logements.length > 0) {
          this.logementActif = logements[0];
          this.chargerRecettes();
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
        this.documents = docs.filter(d => d.typeDocument === 'RELEVE_PLATEFORME' || d.typeDocument === 'AUTRE');
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

  chargerRecettes(): void {
    if (!this.logementActif || !this.logementActif.id) return;

    this.chargement = true;
    this.recetteService.getRecettes(this.logementActif.id).subscribe({
      next: (data) => {
        this.recettes = data.sort((a, b) => {
          const dateA = a.dateEncaissement ? new Date(a.dateEncaissement).getTime() : 0;
          const dateB = b.dateEncaissement ? new Date(b.dateEncaissement).getTime() : 0;
          return dateB - dateA; // Décroissant
        });
        this.chargement = false;
      },
      error: () => {
        this.messageErreur = 'Erreur lors du chargement des recettes.';
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

  chargerFormulaireEdition(recette: Recette): void {
    this.modeEdition = true;
    this.recetteIdEnCours = recette.id || null;
    this.nouvelleRecette = { ...recette };
    this.methodeJustificatif = 'lier';
    this.fichierSelectionne = null;
    this.afficherFormulaire = true;

    if (this.nouvelleRecette.dateDebutSejour) {
      this.nouvelleRecette.dateDebutSejour = this.nouvelleRecette.dateDebutSejour.split('T')[0];
    }
    if (this.nouvelleRecette.dateFinSejour) {
      this.nouvelleRecette.dateFinSejour = this.nouvelleRecette.dateFinSejour.split('T')[0];
    }
    if (this.nouvelleRecette.dateEncaissement) {
      this.nouvelleRecette.dateEncaissement = this.nouvelleRecette.dateEncaissement.split('T')[0];
    }
  }

  annulerEdition(): void {
    this.modeEdition = false;
    this.recetteIdEnCours = null;
    this.nouvelleRecette = this.getResetRecetteForm();
    this.methodeJustificatif = 'lier';
    this.fichierSelectionne = null;
  }

  onFichierSelectionne(event: any): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.fichierSelectionne = input.files[0];
    }
  }

  // Calcul temps réel du montant net en frontend
  get montantNetCalcule(): number {
    const brut = this.nouvelleRecette.montantBrut || 0;
    const frais = this.nouvelleRecette.fraisPlateforme || 0;
    const taxe = this.nouvelleRecette.montantTaxeSejour || 0;
    return brut - frais - taxe;
  }

  calculerNuits(): void {
    if (this.nouvelleRecette.dateDebutSejour && this.nouvelleRecette.dateFinSejour) {
      const debut = new Date(this.nouvelleRecette.dateDebutSejour);
      const fin = new Date(this.nouvelleRecette.dateFinSejour);
      const diffTime = Math.abs(fin.getTime() - debut.getTime());
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      this.nouvelleRecette.nombreNuits = diffDays > 0 ? diffDays : 1;
    }
  }

  onSubmit(): void {
    if (!this.logementActif || !this.logementActif.id) return;

    this.sauvegardeEnCours = true;
    this.messageSucces = null;
    this.messageErreur = null;

    this.nouvelleRecette.logement = { id: this.logementActif.id };
    this.nouvelleRecette.montantNetRecu = this.montantNetCalcule;

    // Téléverser d'abord si sélectionné
    if (this.methodeJustificatif === 'televerser' && this.fichierSelectionne) {
      this.docService.televerser(this.fichierSelectionne, this.logementActif.id, 'RELEVE_PLATEFORME').subscribe({
        next: (doc) => {
          this.nouvelleRecette.documentJustificatif = doc;
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
    if (this.modeEdition && this.recetteIdEnCours) {
      this.recetteService.updateRecette(this.recetteIdEnCours, this.nouvelleRecette).subscribe({
        next: () => {
          this.sauvegardeEnCours = false;
          this.messageSucces = 'Recette modifiée avec succès !';
          this.afficherFormulaire = false;
          this.annulerEdition();
          this.chargerRecettes();
          this.chargerDocuments();
          setTimeout(() => this.messageSucces = null, 3000);
        },
        error: () => {
          this.sauvegardeEnCours = false;
          this.messageErreur = "Une erreur est survenue lors de la modification.";
        }
      });
    } else {
      this.recetteService.saveRecette(this.nouvelleRecette).subscribe({
        next: () => {
          this.sauvegardeEnCours = false;
          this.messageSucces = 'Recette ajoutée avec succès !';
          this.afficherFormulaire = false;
          this.annulerEdition();
          this.chargerRecettes();
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
    if (!id || !confirm('Voulez-vous vraiment supprimer cette recette ?')) return;

    this.recetteService.deleteRecette(id).subscribe({
      next: () => {
        this.messageSucces = 'Recette supprimée avec succès.';
        this.chargerRecettes();
        setTimeout(() => this.messageSucces = null, 3000);
      },
      error: () => {
        this.messageErreur = 'Erreur lors de la suppression.';
      }
    });
  }
}
