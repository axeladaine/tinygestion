import { Component, Input, Output, EventEmitter, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Logement } from '../../models/logement.model';
import { LogementService } from '../../services/logement.service';

@Component({
  selector: 'app-onboarding-initialisation',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './onboarding-initialisation.component.html',
  styleUrls: ['./onboarding-initialisation.component.css']
})
export class OnboardingInitialisationComponent {
  private logementService = inject(LogementService);

  @Input() logement!: Logement;
  @Output() onboardingTermine = new EventEmitter<Logement>();

  etapeActive: number = 1;
  sauvegardeEnCours: boolean = false;
  
  recettesAnterieures: number = 0;
  depensesAnterieures: number = 0;

  anneeCourante: number = new Date().getFullYear();

  suivant(): void {
    if (this.etapeActive < 2) {
      this.etapeActive++;
    }
  }

  precedent(): void {
    if (this.etapeActive > 1) {
      this.etapeActive--;
    }
  }

  passerDidacticiel(): void {
    this.sauvegardeEnCours = true;
    this.logementService.initialiserLogement(this.logement.id!, { recettesAnterieures: 0, depensesAnterieures: 0 })
      .subscribe({
        next: (logementMisAJour) => {
          this.sauvegardeEnCours = false;
          this.onboardingTermine.emit(logementMisAJour);
        },
        error: (err) => {
          this.sauvegardeEnCours = false;
          console.error("Erreur lors de l'initialisation", err);
        }
      });
  }

  enregistrerEtTerminer(): void {
    this.sauvegardeEnCours = true;
    this.logementService.initialiserLogement(this.logement.id!, {
      recettesAnterieures: this.recettesAnterieures || 0,
      depensesAnterieures: this.depensesAnterieures || 0
    }).subscribe({
      next: (logementMisAJour) => {
        this.sauvegardeEnCours = false;
        this.onboardingTermine.emit(logementMisAJour);
      },
      error: (err) => {
        this.sauvegardeEnCours = false;
        console.error("Erreur lors de l'initialisation", err);
      }
    });
  }
}
