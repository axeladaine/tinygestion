import { Component, OnInit, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { LogementService } from '../../services/logement.service';
import { TableauDeBordService, StatsMensuelles } from '../../services/tableau-de-bord.service';
import { RecetteService } from '../../services/recette.service';
import { DepenseService } from '../../services/depense.service';
import { BienAmortissableService } from '../../services/bien-amortissable.service';
import { Logement } from '../../models/logement.model';
import { Recette } from '../../models/recette.model';
import { Depense } from '../../models/depense.model';
import { BienAmortissable } from '../../models/bien-amortissable.model';

@Component({
  selector: 'app-tableau-de-bord',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './tableau-de-bord.component.html',
  styleUrls: ['./tableau-de-bord.component.css']
})
export class TableauDeBordComponent implements OnInit {
  private authService = inject(AuthService);
  private logementService = inject(LogementService);
  private dashboardService = inject(TableauDeBordService);
  private recetteService = inject(RecetteService);
  private depenseService = inject(DepenseService);
  private bienService = inject(BienAmortissableService);
  private router = inject(Router);

  utilisateur = this.authService.currentUser;
  logementSelectionne: Logement | null = null;
  stats: StatsMensuelles | null = null;
  
  chargement: boolean = true;
  chargementStats: boolean = false;
  messageErreur: string | null = null;

  moisSelectionne: number = new Date().getMonth() + 1;
  anneeSelectionnee: number = new Date().getFullYear();

  listeMois = [
    { value: 1, label: 'Janvier' },
    { value: 2, label: 'Février' },
    { value: 3, label: 'Mars' },
    { value: 4, label: 'Avril' },
    { value: 5, label: 'Mai' },
    { value: 6, label: 'Juin' },
    { value: 7, label: 'Juillet' },
    { value: 8, label: 'Août' },
    { value: 9, label: 'Septembre' },
    { value: 10, label: 'Octobre' },
    { value: 11, label: 'Novembre' },
    { value: 12, label: 'Décembre' }
  ];

  listeAnnees: number[] = [];

  // Données de suivi / onboarding
  recettes: Recette[] = [];
  depenses: Depense[] = [];
  biens: BienAmortissable[] = [];

  recettesSansJustificatif: Recette[] = [];
  depensesSansJustificatif: Depense[] = [];

  ngOnInit(): void {
    const anneeCourante = new Date().getFullYear();
    for (let a = anneeCourante - 2; a <= anneeCourante + 1; a++) {
      this.listeAnnees.push(a);
    }
    this.chargerLogements();
  }

  chargerLogements(): void {
    this.chargement = true;
    this.logementService.getLogements().subscribe({
      next: (logements) => {
        if (logements && logements.length > 0) {
          this.logementSelectionne = logements[0];
          this.chargerStats();
          this.chargerDonneesSuivi();
        } else {
          this.chargement = false;
        }
      },
      error: () => {
        this.messageErreur = 'Impossible de charger vos hébergements.';
        this.chargement = false;
      }
    });
  }

  chargerStats(): void {
    if (!this.logementSelectionne || !this.logementSelectionne.id) {
      this.chargement = false;
      return;
    }

    this.chargementStats = true;
    this.messageErreur = null;

    this.dashboardService.getStatsMensuelles(
      this.logementSelectionne.id,
      this.moisSelectionne,
      this.anneeSelectionnee
    ).subscribe({
      next: (data) => {
        this.stats = data;
        this.chargementStats = false;
        this.chargement = false;
      },
      error: () => {
        this.messageErreur = 'Erreur lors du calcul des statistiques mensuelles.';
        this.chargementStats = false;
        this.chargement = false;
      }
    });
  }

  chargerDonneesSuivi(): void {
    if (!this.logementSelectionne || !this.logementSelectionne.id) return;
    const lid = this.logementSelectionne.id;

    // Charger recettes
    this.recetteService.getRecettes(lid).subscribe({
      next: (data) => {
        this.recettes = data;
        this.recettesSansJustificatif = data.filter(r => !r.documentJustificatif);
      }
    });

    // Charger dépenses
    this.depenseService.getDepenses(lid).subscribe({
      next: (data) => {
        this.depenses = data;
        this.depensesSansJustificatif = data.filter(d => !d.documentJustificatif);
      }
    });

    // Charger biens
    this.bienService.getBiens(lid).subscribe({
      next: (data) => {
        this.biens = data;
      }
    });
  }

  onPeriodeChange(): void {
    this.chargerStats();
  }

  moisPrecedent(): void {
    if (this.moisSelectionne === 1) {
      this.moisSelectionne = 12;
      this.anneeSelectionnee--;
      if (!this.listeAnnees.includes(this.anneeSelectionnee)) {
        this.listeAnnees.unshift(this.anneeSelectionnee);
      }
    } else {
      this.moisSelectionne--;
    }
    this.chargerStats();
  }

  moisSuivant(): void {
    if (this.moisSelectionne === 12) {
      this.moisSelectionne = 1;
      this.anneeSelectionnee++;
      if (!this.listeAnnees.includes(this.anneeSelectionnee)) {
        this.listeAnnees.push(this.anneeSelectionnee);
      }
    } else {
      this.moisSelectionne++;
    }
    this.chargerStats();
  }

  // Éléments du parcours d'accompagnement (onboarding)
  get step1Complete(): boolean {
    return this.logementSelectionne !== null && !!this.logementSelectionne.nom;
  }

  get step2Complete(): boolean {
    return this.biens.length > 0;
  }

  get step3Complete(): boolean {
    return this.recettes.length > 0;
  }

  get step4Complete(): boolean {
    return this.depenses.length > 0;
  }

  get onboardingTermine(): boolean {
    return this.step1Complete && this.step2Complete && this.step3Complete && this.step4Complete;
  }

  deconnexion(): void {
    this.authService.deconnexion();
    this.router.navigate(['/connexion']);
  }
}
