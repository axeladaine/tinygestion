import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { BilanFiscalService } from '../../services/bilan-fiscal.service';
import { LogementService } from '../../services/logement.service';
import { Logement } from '../../models/logement.model';
import { BilanFiscal } from '../../models/bilan-fiscal.model';

@Component({
  selector: 'app-bilan-annuel',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './bilan-annuel.component.html',
  styleUrls: ['./bilan-annuel.component.css']
})
export class BilanAnnuelComponent implements OnInit {
  private bilanService = inject(BilanFiscalService);
  private logementService = inject(LogementService);

  logementActif: Logement | null = null;
  bilan: BilanFiscal | null = null;
  chargement: boolean = true;
  messageErreur: string | null = null;
  afficherModalPdf: boolean = false;

  // Filtres
  anneeSelectionnee: number = new Date().getFullYear();
  listeAnnees: number[] = [2025, 2026, 2027, 2028, 2029, 2030];

  ngOnInit(): void {
    this.chargerLogement();
  }

  chargerLogement(): void {
    this.logementService.getLogements().subscribe({
      next: (logements) => {
        if (logements && logements.length > 0) {
          this.logementActif = logements[0];
          this.chargerBilan();
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

  chargerBilan(): void {
    if (!this.logementActif || !this.logementActif.id) return;
    this.chargement = true;
    this.messageErreur = null;

    this.bilanService.getBilanFiscal(this.logementActif.id, this.anneeSelectionnee).subscribe({
      next: (data) => {
        this.bilan = data;
        this.chargement = false;
      },
      error: () => {
        this.messageErreur = 'Erreur lors du calcul du bilan fiscal annuel.';
        this.chargement = false;
      }
    });
  }

  onAnneeChange(): void {
    this.chargerBilan();
  }

  afficherAidePdf(): void {
    if (!this.logementActif || !this.logementActif.id) return;
    this.afficherModalPdf = true;
  }

  fermerModalPdf(): void {
    this.afficherModalPdf = false;
  }
}
