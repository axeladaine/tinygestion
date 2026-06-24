import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-guide-fiscal',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './guide-fiscal.component.html',
  styleUrls: ['./guide-fiscal.component.css']
})
export class GuideFiscalComponent {
  ongletActif: 'lmnp' | 'comparatif' | 'amortissement' | 'cfe' | 'calendrier' | 'declaration' | 'faq' = 'lmnp';

  changerOnglet(onglet: 'lmnp' | 'comparatif' | 'amortissement' | 'cfe' | 'calendrier' | 'declaration' | 'faq'): void {
    this.ongletActif = onglet;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
}
