import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

export interface Utilisateur {
  id: string;
  nom: string;
  prenom?: string;
  email: string;
  role: string;
}

export interface AuthResponse {
  token: string;
  utilisateur: Utilisateur;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  
  // Signal pour l'utilisateur actuellement connecté
  private currentUserSignal = signal<Utilisateur | null>(null);
  
  // Sécurisation de l'accès au signal en lecture seule
  public readonly currentUser = computed(() => this.currentUserSignal());
  public readonly isAuthenticated = computed(() => this.currentUserSignal() !== null);

  constructor() {
    this.restaurerSession();
  }

  connexion(email: string, motDePasse: string): Observable<AuthResponse> {
    const url = '/api/authentification/connexion';
    return this.http.post<AuthResponse>(url, { email, motDePasse }).pipe(
      tap((reponse) => {
        this.sauvegarderSession(reponse);
      })
    );
  }

  inscription(payload: {
    email: string;
    motDePasse: string;
    prenom: string;
    nom: string;
    nomLogement: string;
    adresseLogement?: string;
    codePostalLogement?: string;
    villeLogement?: string;
  }): Observable<AuthResponse> {
    const url = '/api/authentification/inscription';
    return this.http.post<AuthResponse>(url, payload).pipe(
      tap((reponse) => {
        this.sauvegarderSession(reponse);
      })
    );
  }

  deconnexion(): void {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('utilisateur_session');
    this.currentUserSignal.set(null);
    this.router.navigate(['/connexion']);
  }

  private sauvegarderSession(reponse: AuthResponse): void {
    localStorage.setItem('jwt_token', reponse.token);
    localStorage.setItem('utilisateur_session', JSON.stringify(reponse.utilisateur));
    this.currentUserSignal.set(reponse.utilisateur);
  }

  private restaurerSession(): void {
    const token = localStorage.getItem('jwt_token');
    const userJson = localStorage.getItem('utilisateur_session');

    if (token && userJson) {
      try {
        const utilisateur = JSON.parse(userJson) as Utilisateur;
        this.currentUserSignal.set(utilisateur);
      } catch (e) {
        this.deconnexion();
      }
    }
  }

  getToken(): string | null {
    return localStorage.getItem('jwt_token');
  }
}
