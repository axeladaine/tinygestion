import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FlatpickrDirective } from 'angularx-flatpickr';
import { LogementService } from '../../services/logement.service';
import { Logement } from '../../models/logement.model';

@Component({
  selector: 'app-fiche-logement',
  standalone: true,
  imports: [CommonModule, FormsModule, FlatpickrDirective],
  templateUrl: './fiche-logement.component.html',
  styleUrls: ['./fiche-logement.component.css']
})
export class FicheLogementComponent implements OnInit {
  private logementService = inject(LogementService);

  logement: Logement | null = null;
  messageSucces: string | null = null;
  messageErreur: string | null = null;
  chargement: boolean = true;
  sauvegardeEnCours: boolean = false;

  qualifications = [
    { value: 'RESIDENCE_SECONDAIRE', label: 'Résidence secondaire' },
    { value: 'MEUBLE_DE_TOURISME', label: 'Meublé de tourisme' },
    { value: 'DEPENDANCE', label: 'Dépendance' },
    { value: 'A_VERIFIER', label: 'À vérifier avec les impôts' }
  ];

  ngOnInit(): void {
    this.chargerLogement();
  }

  chargerLogement(): void {
    this.chargement = true;
    this.logementService.getLogements().subscribe({
      next: (logements) => {
        if (logements && logements.length > 0) {
          this.logement = logements[0];
        } else {
          // Création d'un logement par défaut si aucun n'existe
          this.logement = {
            nom: 'Ma Tiny House',
            qualificationLogement: 'A_VERIFIER',
            estDeplacable: true,
            estSurTerrainResidencePrincipale: false,
            estLoueCourteDuree: true,
            estMeubleTourisme: false
          };
        }
        this.chargement = false;
      },
      error: (err) => {
        this.messageErreur = 'Erreur lors du chargement de la fiche logement.';
        this.chargement = false;
      }
    });
  }

  onSubmit(): void {
    if (!this.logement) return;

    this.sauvegardeEnCours = true;
    this.messageSucces = null;
    this.messageErreur = null;

    const saveObservable = this.logement.id 
      ? this.logementService.updateLogement(this.logement.id, this.logement)
      : this.logementService.saveLogement(this.logement);

    saveObservable.subscribe({
      next: (savedLogement) => {
        this.logement = savedLogement;
        this.sauvegardeEnCours = false;
        this.messageSucces = 'La fiche logement a été enregistrée avec succès.';
        // Masquer le message de succès après 3 secondes
        setTimeout(() => this.messageSucces = null, 3000);
      },
      error: (err) => {
        this.sauvegardeEnCours = false;
        this.messageErreur = 'Erreur lors de la sauvegarde du logement.';
      }
    });
  }
}
