import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FlatpickrDirective } from 'angularx-flatpickr';
import { BienAmortissableService } from '../../services/bien-amortissable.service';
import { LogementService } from '../../services/logement.service';
import { DocumentJustificatifService } from '../../services/document-justificatif.service';
import { BienAmortissable } from '../../models/bien-amortissable.model';
import { Logement } from '../../models/logement.model';
import { DocumentJustificatif } from '../../models/document-justificatif.model';

@Component({
  selector: 'app-biens-amortissables',
  standalone: true,
  imports: [CommonModule, FormsModule, FlatpickrDirective],
  templateUrl: './biens-amortissables.component.html',
  styleUrls: ['./biens-amortissables.component.css']
})
export class BiensAmortissablesComponent implements OnInit {
  private bienService = inject(BienAmortissableService);
  private logementService = inject(LogementService);
  private docService = inject(DocumentJustificatifService);

  logementActif: Logement | null = null;
  biens: BienAmortissable[] = [];
  documents: DocumentJustificatif[] = [];
  chargement: boolean = true;
  afficherFormulaire: boolean = false;
  messageSucces: string | null = null;
  messageErreur: string | null = null;
  sauvegardeEnCours: boolean = false;

  // Formulaire
  nouveauBien: BienAmortissable = this.getResetBienForm();
  categories = [
    { value: 'TINY_HOUSE', label: 'Tiny House (structure)' },
    { value: 'TERRASSE', label: 'Terrasse' },
    { value: 'RACCORDEMENT', label: 'Raccordement (eau, élec, assainissement)' },
    { value: 'MOBILIER', label: 'Mobilier' },
    { value: 'LITERIE', label: 'Literie' },
    { value: 'ELECTROMENAGER', label: 'Électroménager' },
    { value: 'CHAUFFAGE_CLIMATISATION', label: 'Chauffage / Climatisation' },
    { value: 'DECORATION', label: 'Décoration' },
    { value: 'AUTRE', label: 'Autre équipement durable' }
  ];

  ngOnInit(): void {
    this.chargerLogements();
  }

  getResetBienForm(): BienAmortissable {
    return {
      logement: {},
      nom: '',
      categorie: 'TINY_HOUSE',
      dateAchat: new Date().toISOString().split('T')[0],
      dateMiseEnService: new Date().toISOString().split('T')[0],
      montantTtc: 0,
      tauxAffectationLocation: 100,
      dureeAmortissementAns: 10,
      possedeFacture: false,
      commentaire: ''
    };
  }

  chargerLogements(): void {
    this.logementService.getLogements().subscribe({
      next: (logements) => {
        if (logements && logements.length > 0) {
          this.logementActif = logements[0];
          this.chargerBiens();
          this.chargerDocuments();
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

  chargerBiens(): void {
    if (!this.logementActif || !this.logementActif.id) return;
    this.chargement = true;
    this.bienService.getBiens(this.logementActif.id).subscribe({
      next: (data) => {
        this.biens = data;
        this.chargement = false;
      },
      error: () => {
        this.messageErreur = 'Erreur lors du chargement des biens amortissables.';
        this.chargement = false;
      }
    });
  }

  chargerDocuments(): void {
    if (!this.logementActif || !this.logementActif.id) return;
    this.docService.getDocuments(this.logementActif.id).subscribe({
      next: (data) => {
        this.documents = data.filter(d => d.typeDocument === 'FACTURE' || d.typeDocument === 'AUTRE');
      }
    });
  }

  toggleFormulaire(): void {
    this.afficherFormulaire = !this.afficherFormulaire;
    if (!this.afficherFormulaire) {
      this.nouveauBien = this.getResetBienForm();
    }
  }

  // Calculs réactifs en frontend
  get baseAmortissableCalculee(): number {
    const ttc = this.nouveauBien.montantTtc || 0;
    const taux = this.nouveauBien.tauxAffectationLocation || 0;
    return Number((ttc * taux / 100).toFixed(2));
  }

  get amortissementAnnuelCalcule(): number {
    const base = this.baseAmortissableCalculee;
    const ans = this.nouveauBien.dureeAmortissementAns || 1;
    return Number((base / ans).toFixed(2));
  }

  get amortissementMensuelCalcule(): number {
    return Number((this.amortissementAnnuelCalcule / 12).toFixed(2));
  }

  // Cumul global d'amortissement estimatif pour l'affichage général
  get totalAmortissementsAnnuels(): number {
    return this.biens.reduce((sum, item) => sum + (item.amortissementAnnuel || 0), 0);
  }

  get totalAmortissementsMensuels(): number {
    return this.biens.reduce((sum, item) => sum + (item.amortissementMensuel || 0), 0);
  }

  onSubmit(): void {
    if (!this.logementActif || !this.logementActif.id) return;

    this.sauvegardeEnCours = true;
    this.messageSucces = null;
    this.messageErreur = null;

    this.nouveauBien.logement = { id: this.logementActif.id };
    this.nouveauBien.baseAmortissable = this.baseAmortissableCalculee;
    this.nouveauBien.amortissementAnnuel = this.amortissementAnnuelCalcule;
    this.nouveauBien.amortissementMensuel = this.amortissementMensuelCalcule;

    this.bienService.saveBien(this.nouveauBien).subscribe({
      next: () => {
        this.sauvegardeEnCours = false;
        this.messageSucces = 'Bien amortissable ajouté avec succès !';
        this.toggleFormulaire();
        this.chargerBiens();
        setTimeout(() => this.messageSucces = null, 3000);
      },
      error: () => {
        this.sauvegardeEnCours = false;
        this.messageErreur = 'Erreur lors de la sauvegarde du bien amortissable.';
      }
    });
  }

  supprimer(id: number | undefined): void {
    if (!id || !confirm('Voulez-vous vraiment supprimer ce bien amortissable ?')) return;

    this.bienService.deleteBien(id).subscribe({
      next: () => {
        this.messageSucces = 'Bien amortissable supprimé.';
        this.chargerBiens();
        setTimeout(() => this.messageSucces = null, 3000);
      },
      error: () => {
        this.messageErreur = 'Erreur lors de la suppression.';
      }
    });
  }

  getLabelCategorie(cat: string): string {
    const matched = this.categories.find(c => c.value === cat);
    return matched ? matched.label : cat;
  }
}
