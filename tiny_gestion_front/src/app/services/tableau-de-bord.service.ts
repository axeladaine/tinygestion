import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface StatsMensuelles {
  totalRecettes: number;
  totalDepenses: number;
  resultatEstime: number;
  justificatifsManquants: number;
}

@Injectable({
  providedIn: 'root'
})
export class TableauDeBordService {
  private http = inject(HttpClient);
  private url = '/api/tableau-de-bord';

  getStatsMensuelles(logementId: number, mois: number, annee: number): Observable<StatsMensuelles> {
    return this.http.get<StatsMensuelles>(`${this.url}/logement/${logementId}?mois=${mois}&annee=${annee}`);
  }
}
