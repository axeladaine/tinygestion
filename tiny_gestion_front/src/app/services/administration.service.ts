import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { JournalSecurite } from '../models/journal-securite.model';

@Injectable({
  providedIn: 'root'
})
export class AdministrationService {
  private http = inject(HttpClient);
  private url = '/api/administration';

  getJournalSecurite(): Observable<JournalSecurite[]> {
    return this.http.get<JournalSecurite[]>(`${this.url}/journal-securite`);
  }

  creerAssistant(assistant: { email: string; motDePasse: string; prenom: string; nom: string }): Observable<any> {
    return this.http.post<any>(`${this.url}/assistant`, assistant);
  }

  getAssistants(): Observable<any[]> {
    return this.http.get<any[]>(`${this.url}/assistant`);
  }

  supprimerAssistant(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/assistant/${id}`);
  }
}
