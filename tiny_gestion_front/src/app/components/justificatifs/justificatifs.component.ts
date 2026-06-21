import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DocumentJustificatifService } from '../../services/document-justificatif.service';
import { LogementService } from '../../services/logement.service';
import { DocumentJustificatif } from '../../models/document-justificatif.model';
import { Logement } from '../../models/logement.model';
import { RecetteService } from '../../services/recette.service';
import { DepenseService } from '../../services/depense.service';
import { Recette } from '../../models/recette.model';
import { Depense } from '../../models/depense.model';

@Component({
  selector: 'app-justificatifs',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './justificatifs.component.html',
  styleUrls: ['./justificatifs.component.css']
})
export class JustificatifsComponent implements OnInit {
  private docService = inject(DocumentJustificatifService);
  private logementService = inject(LogementService);
  private recetteService = inject(RecetteService);
  private depenseService = inject(DepenseService);

  // Variables de liaison
  documentALier: DocumentJustificatif | null = null;
  typeLiaison: 'RECETTE' | 'DEPENSE' = 'RECETTE';
  operationSelectionneeId: number | null = null;
  recettesSansJustif: Recette[] = [];
  depensesSansJustif: Depense[] = [];

  logementActif: Logement | null = null;
  documents: DocumentJustificatif[] = [];
  chargement: boolean = true;
  messageSucces: string | null = null;
  messageErreur: string | null = null;

  // Formulaire d'upload
  fichierSelectionne: File | null = null;
  typeDocumentSelectionne: 'FACTURE' | 'RELEVE_PLATEFORME' | 'DOCUMENT_FISCAL' | 'DOCUMENT_MAIRIE' | 'ASSURANCE' | 'IMPOT_LOCAL' | 'AUTRE' = 'FACTURE';
  sauvegardeEnCours: boolean = false;

  typesDocument = [
    { value: 'FACTURE', label: 'Facture d\'achat/entretien' },
    { value: 'RELEVE_PLATEFORME', label: 'Relevé de revenus (Airbnb, Booking...)' },
    { value: 'DOCUMENT_FISCAL', label: 'Document fiscal' },
    { value: 'DOCUMENT_MAIRIE', label: 'Document mairie / urbanisme' },
    { value: 'ASSURANCE', label: 'Contrat d\'assurance' },
    { value: 'IMPOT_LOCAL', label: 'Avis d\'impôt local (CFE...)' },
    { value: 'AUTRE', label: 'Autre justificatif' }
  ];

  ngOnInit(): void {
    this.chargerLogements();
  }

  chargerLogements(): void {
    this.logementService.getLogements().subscribe({
      next: (logements) => {
        if (logements && logements.length > 0) {
          this.logementActif = logements[0];
          this.chargerDocuments();
          this.chargerOperations();
        } else {
          this.chargement = false;
        }
      },
      error: () => {
        this.messageErreur = 'Erreur lors du chargement du logement.';
        this.chargement = false;
      }
    });
  }

  chargerOperations(): void {
    if (!this.logementActif || !this.logementActif.id) return;
    
    this.recetteService.getRecettes(this.logementActif.id).subscribe({
      next: (recettes) => {
        this.recettesSansJustif = recettes.filter(r => !r.documentJustificatifId && !r.documentJustificatif);
      }
    });
    
    this.depenseService.getDepenses(this.logementActif.id).subscribe({
      next: (depenses) => {
        this.depensesSansJustif = depenses.filter(d => !d.documentJustificatifId && !d.documentJustificatif);
      }
    });
  }

  chargerDocuments(): void {
    if (!this.logementActif || !this.logementActif.id) return;
    this.chargement = true;
    this.docService.getDocuments(this.logementActif.id).subscribe({
      next: (data) => {
        this.documents = data.sort((a, b) => {
          const dateA = a.dateTeleversement ? new Date(a.dateTeleversement).getTime() : 0;
          const dateB = b.dateTeleversement ? new Date(b.dateTeleversement).getTime() : 0;
          return dateB - dateA; // Plus récent en premier
        });
        this.chargement = false;
      },
      error: () => {
        this.messageErreur = 'Erreur lors du chargement des documents.';
        this.chargement = false;
      }
    });
  }

  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (file) {
      this.fichierSelectionne = file;
    }
  }

  onSubmit(): void {
    if (!this.logementActif || !this.logementActif.id || !this.fichierSelectionne) {
      this.messageErreur = 'Veuillez sélectionner un fichier à téléverser.';
      return;
    }

    this.sauvegardeEnCours = true;
    this.messageSucces = null;
    this.messageErreur = null;

    this.docService.televerser(
      this.fichierSelectionne,
      this.logementActif.id,
      this.typeDocumentSelectionne
    ).subscribe({
      next: () => {
        this.sauvegardeEnCours = false;
        this.messageSucces = 'Document téléversé et enregistré avec succès !';
        this.fichierSelectionne = null;
        // Reset file input value
        const fileInput = document.getElementById('fichierInput') as HTMLInputElement;
        if (fileInput) fileInput.value = '';
        this.chargerDocuments();
        setTimeout(() => this.messageSucces = null, 3000);
      },
      error: (err) => {
        this.sauvegardeEnCours = false;
        if (err.error && typeof err.error === 'string') {
          this.messageErreur = err.error;
        } else {
          this.messageErreur = 'Erreur lors du téléversement du fichier.';
        }
      }
    });
  }

  telecharger(doc: DocumentJustificatif): void {
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
        this.messageErreur = 'Erreur lors du téléchargement du fichier.';
      }
    });
  }

  supprimer(id: number | undefined): void {
    if (!id || !confirm('Voulez-vous vraiment supprimer ce document ? Cela annulera ses liaisons.')) return;

    this.docService.deleteDocument(id).subscribe({
      next: () => {
        this.messageSucces = 'Document supprimé avec succès.';
        this.chargerDocuments();
        setTimeout(() => this.messageSucces = null, 3000);
      },
      error: () => {
        this.messageErreur = 'Erreur lors de la suppression du document.';
      }
    });
  }

  getLabelType(type: string): string {
    const matched = this.typesDocument.find(t => t.value === type);
    return matched ? matched.label : type;
  }

  ouvrirLiaison(doc: DocumentJustificatif): void {
    this.documentALier = doc;
    this.typeLiaison = 'RECETTE';
    this.operationSelectionneeId = null;
    this.chargerOperations();
    setTimeout(() => {
      this.mettreAJourOperationParDefaut();
    }, 200);
  }

  mettreAJourOperationParDefaut(): void {
    if (this.typeLiaison === 'RECETTE' && this.recettesSansJustif.length > 0) {
      this.operationSelectionneeId = this.recettesSansJustif[0].id || null;
    } else if (this.typeLiaison === 'DEPENSE' && this.depensesSansJustif.length > 0) {
      this.operationSelectionneeId = this.depensesSansJustif[0].id || null;
    } else {
      this.operationSelectionneeId = null;
    }
  }

  changerTypeLiaison(type: 'RECETTE' | 'DEPENSE'): void {
    this.typeLiaison = type;
    this.mettreAJourOperationParDefaut();
  }

  annulerLiaison(): void {
    this.documentALier = null;
    this.operationSelectionneeId = null;
  }

  confirmerLiaison(): void {
    if (!this.documentALier || !this.operationSelectionneeId) {
      this.messageErreur = 'Veuillez sélectionner une opération valide.';
      return;
    }

    this.sauvegardeEnCours = true;
    this.messageSucces = null;
    this.messageErreur = null;

    if (this.typeLiaison === 'RECETTE') {
      const recette = this.recettesSansJustif.find(r => r.id === this.operationSelectionneeId);
      if (recette) {
        recette.documentJustificatif = this.documentALier;
        this.recetteService.updateRecette(recette.id!, recette).subscribe({
          next: () => {
            this.sauvegardeEnCours = false;
            this.messageSucces = 'Le justificatif a été associé à la recette avec succès !';
            this.documentALier = null;
            this.chargerDocuments();
            this.chargerOperations();
            setTimeout(() => this.messageSucces = null, 3000);
          },
          error: () => {
            this.sauvegardeEnCours = false;
            this.messageErreur = 'Erreur lors de la liaison à la recette.';
          }
        });
      }
    } else if (this.typeLiaison === 'DEPENSE') {
      const depense = this.depensesSansJustif.find(d => d.id === this.operationSelectionneeId);
      if (depense) {
        depense.documentJustificatif = this.documentALier;
        this.depenseService.updateDepense(depense.id!, depense).subscribe({
          next: () => {
            this.sauvegardeEnCours = false;
            this.messageSucces = 'Le justificatif a été associé à la dépense avec succès !';
            this.documentALier = null;
            this.chargerDocuments();
            this.chargerOperations();
            setTimeout(() => this.messageSucces = null, 3000);
          },
          error: () => {
            this.sauvegardeEnCours = false;
            this.messageErreur = 'Erreur lors de la liaison à la dépense.';
          }
        });
      }
    }
  }
}
