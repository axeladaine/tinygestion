import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ChecklistMensuelleService } from '../../services/checklist-mensuelle.service';
import { LogementService } from '../../services/logement.service';
import { Logement } from '../../models/logement.model';
import { ChecklistMensuelle, TacheChecklist } from '../../models/checklist-mensuelle.model';

@Component({
  selector: 'app-checklists',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './checklists.component.html',
  styleUrls: ['./checklists.component.css']
})
export class ChecklistsComponent implements OnInit {
  private checklistService = inject(ChecklistMensuelleService);
  private logementService = inject(LogementService);

  logementActif: Logement | null = null;
  checklist: ChecklistMensuelle | null = null;
  taches: TacheChecklist[] = [];
  chargement: boolean = true;
  messageSucces: string | null = null;
  messageErreur: string | null = null;
  sauvegardeEnCours: boolean = false;

  // Filtres
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

  listeAnnees: number[] = [2025, 2026, 2027, 2028, 2029, 2030];

  ngOnInit(): void {
    this.chargerLogement();
  }

  chargerLogement(): void {
    this.logementService.getLogements().subscribe({
      next: (logements) => {
        if (logements && logements.length > 0) {
          this.logementActif = logements[0];
          this.chargerChecklist();
        } else {
          this.chargement = false;
        }
      },
      error: () => {
        this.messageErreur = 'Erreur lors du chargement de la Tiny House.';
        this.chargement = false;
      }
    });
  }

  chargerChecklist(): void {
    if (!this.logementActif || !this.logementActif.id) return;
    this.chargement = true;
    this.messageErreur = null;

    this.checklistService.getOrCreateChecklist(
      this.logementActif.id,
      this.anneeSelectionnee,
      this.moisSelectionne
    ).subscribe({
      next: (data) => {
        this.checklist = data;
        try {
          this.taches = JSON.parse(data.tachesJson) as TacheChecklist[];
        } catch (e) {
          this.taches = [];
          this.messageErreur = 'Erreur de lecture des tâches de la checklist.';
        }
        this.chargement = false;
      },
      error: () => {
        this.messageErreur = 'Erreur lors du chargement de la checklist.';
        this.chargement = false;
      }
    });
  }

  onPeriodeChange(): void {
    this.chargerChecklist();
  }

  toggleTache(tache: TacheChecklist): void {
    if (!this.checklist || !this.checklist.id) return;

    tache.termine = !tache.termine;
    this.sauvegardeEnCours = true;

    const tachesStringify = JSON.stringify(this.taches);

    this.checklistService.updateChecklist(this.checklist.id, tachesStringify).subscribe({
      next: (updated) => {
        this.checklist = updated;
        this.sauvegardeEnCours = false;
        // Optionnel : Notification silencieuse ou message court
      },
      error: () => {
        // Rétablir la valeur locale en cas d'erreur
        tache.termine = !tache.termine;
        this.sauvegardeEnCours = false;
        this.messageErreur = 'Impossible de sauvegarder la modification.';
      }
    });
  }

  get tauxCompletionCalculer(): number {
    if (this.taches.length === 0) return 0;
    const terminees = this.taches.filter(t => t.termine).length;
    return Math.round((terminees / this.taches.length) * 100);
  }

  getLabelMois(m: number): string {
    const matched = this.listeMois.find(item => item.value === m);
    return matched ? matched.label : m.toString();
  }
}
