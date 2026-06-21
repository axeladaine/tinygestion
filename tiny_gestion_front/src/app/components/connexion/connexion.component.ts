import { Component, inject } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-connexion',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './connexion.component.html',
  styleUrls: ['./connexion.component.css']
})
export class ConnexionComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  email: string = '';
  motDePasse: string = '';
  messageErreur: string | null = null;
  chargement: boolean = false;

  onSubmit(): void {
    if (!this.email || !this.motDePasse) {
      this.messageErreur = 'Veuillez remplir tous les champs.';
      return;
    }

    this.chargement = true;
    this.messageErreur = null;

    this.authService.connexion(this.email, this.motDePasse).subscribe({
      next: () => {
        this.chargement = false;
        this.router.navigate(['/tableau-de-bord']);
      },
      error: (err) => {
        this.chargement = false;
        if (err.status === 401 || err.status === 403) {
          this.messageErreur = 'Email ou mot de passe incorrect.';
        } else {
          this.messageErreur = 'Une erreur est survenue lors de la communication avec le serveur (code ' + err.status + ').';
        }
      }
    });
  }
}
