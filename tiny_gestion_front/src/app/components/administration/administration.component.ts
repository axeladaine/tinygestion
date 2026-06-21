import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdministrationService } from '../../services/administration.service';
import { AuthService } from '../../services/auth.service';
import { JournalSecurite } from '../../models/journal-securite.model';

@Component({
  selector: 'app-administration',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './administration.component.html',
  styleUrls: ['./administration.component.css']
})
export class AdministrationComponent implements OnInit {
  private adminService = inject(AdministrationService);
  public authService = inject(AuthService);

  logs: JournalSecurite[] = [];
  assistants: any[] = [];
  chargementLogs: boolean = true;
  chargementAssistants: boolean = true;

  messageSucces: string | null = null;
  messageErreur: string | null = null;
  sauvegardeEnCours: boolean = false;

  // Formulaire Assistant
  emailAssistant: string = '';
  motDePasseAssistant: string = '';
  prenomAssistant: string = '';
  nomAssistant: string = '';

  ngOnInit(): void {
    if (this.estProprietaire) {
      this.chargerLogs();
      this.chargerAssistants();
    }
  }

  get estProprietaire(): boolean {
    const user = this.authService.currentUser();
    return user !== null && user.role === 'PROPRIETAIRE';
  }

  chargerLogs(): void {
    this.chargementLogs = true;
    this.adminService.getJournalSecurite().subscribe({
      next: (data) => {
        this.logs = data;
        this.chargementLogs = false;
      },
      error: () => {
        this.messageErreur = 'Erreur lors du chargement du journal de sécurité.';
        this.chargementLogs = false;
      }
    });
  }

  chargerAssistants(): void {
    this.chargementAssistants = true;
    this.adminService.getAssistants().subscribe({
      next: (data) => {
        this.assistants = data;
        this.chargementAssistants = false;
      },
      error: () => {
        this.messageErreur = 'Erreur lors du chargement des assistants.';
        this.chargementAssistants = false;
      }
    });
  }

  onSubmitAssistant(): void {
    if (!this.emailAssistant || !this.motDePasseAssistant || !this.prenomAssistant || !this.nomAssistant) {
      this.messageErreur = 'Veuillez remplir tous les champs du formulaire.';
      return;
    }

    this.sauvegardeEnCours = true;
    this.messageSucces = null;
    this.messageErreur = null;

    const payload = {
      email: this.emailAssistant,
      motDePasse: this.motDePasseAssistant,
      prenom: this.prenomAssistant,
      nom: this.nomAssistant
    };

    this.adminService.creerAssistant(payload).subscribe({
      next: () => {
        this.sauvegardeEnCours = false;
        this.messageSucces = 'Le compte assistant a été créé avec succès !';
        this.emailAssistant = '';
        this.motDePasseAssistant = '';
        this.prenomAssistant = '';
        this.nomAssistant = '';
        this.chargerAssistants();
        this.chargerLogs(); // Actualiser le journal de sécurité
        setTimeout(() => this.messageSucces = null, 3000);
      },
      error: (err) => {
        this.sauvegardeEnCours = false;
        if (err.error) {
          this.messageErreur = err.error;
        } else {
          this.messageErreur = 'Erreur lors de la création du compte assistant.';
        }
      }
    });
  }

  supprimerAssistant(id: number): void {
    if (!confirm('Voulez-vous vraiment supprimer cet assistant ? Son accès sera révoqué immédiatement.')) return;

    this.adminService.supprimerAssistant(id).subscribe({
      next: () => {
        this.messageSucces = 'L\'accès de l\'assistant a été supprimé.';
        this.chargerAssistants();
        this.chargerLogs();
        setTimeout(() => this.messageSucces = null, 3000);
      },
      error: () => {
        this.messageErreur = 'Erreur lors de la révocation de l\'assistant.';
      }
    });
  }
}
