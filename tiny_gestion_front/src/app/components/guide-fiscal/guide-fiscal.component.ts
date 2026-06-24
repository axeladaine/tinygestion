import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-guide-fiscal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './guide-fiscal.component.html',
  styleUrls: ['./guide-fiscal.component.css']
})
export class GuideFiscalComponent {
  ongletActif: 'lmnp' | 'comparatif' | 'amortissement' | 'cfe' | 'calendrier' = 'lmnp';

  changerOnglet(onglet: 'lmnp' | 'comparatif' | 'amortissement' | 'cfe' | 'calendrier'): void {
    this.ongletActif = onglet;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
}
