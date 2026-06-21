import { Component, inject } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-inscription',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './inscription.component.html',
  styleUrls: ['./inscription.component.css']
})
export class InscriptionComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  email: string = '';
  motDePasse: string = '';
  confirmationMotDePasse: string = '';
  prenom: string = '';
  nom: string = '';
  nomLogement: string = '';
  adresseLogement: string = '';
  codePostalLogement: string = '';
  villeLogement: string = '';

  messageErreur: string | null = null;
  chargement: boolean = false;

  onSubmit(): void {
    if (!this.email || !this.motDePasse || !this.prenom || !this.nom || !this.nomLogement) {
      this.messageErreur = 'Veuillez remplir tous les champs obligatoires (indiqués par un *).';
      return;
    }

    if (this.motDePasse !== this.confirmationMotDePasse) {
      this.messageErreur = 'Les mots de passe ne correspondent pas.';
      return;
    }

    if (this.motDePasse.length < 8) {
      this.messageErreur = 'Le mot de passe doit faire au moins 8 caractères.';
      return;
    }

    this.chargement = true;
    this.messageErreur = null;

    const payload = {
      email: this.email,
      motDePasse: this.motDePasse,
      prenom: this.prenom,
      nom: this.nom,
      nomLogement: this.nomLogement,
      adresseLogement: this.adresseLogement,
      codePostalLogement: this.codePostalLogement,
      villeLogement: this.villeLogement
    };

    this.authService.inscription(payload).subscribe({
      next: () => {
        this.chargement = false;
        this.router.navigate(['/tableau-de-bord']);
      },
      error: (err) => {
        this.chargement = false;
        if (err.error) {
          this.messageErreur = err.error;
        } else {
          this.messageErreur = 'Une erreur est survenue lors de la communication avec le serveur (code ' + err.status + ').';
        }
      }
    });
  }
}
