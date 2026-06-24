import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Logement } from '../models/logement.model';

@Injectable({
  providedIn: 'root'
})
export class LogementService {
  private http = inject(HttpClient);
  private url = '/api/logements';

  getLogements(): Observable<Logement[]> {
    return this.http.get<Logement[]>(this.url);
  }

  getLogementById(id: number): Observable<Logement> {
    return this.http.get<Logement>(`${this.url}/${id}`);
  }

  saveLogement(logement: Logement): Observable<Logement> {
    return this.http.post<Logement>(this.url, logement);
  }

  updateLogement(id: number, logement: Logement): Observable<Logement> {
    return this.http.put<Logement>(`${this.url}/${id}`, logement);
  }

  initialiserLogement(id: number, data: { recettesAnterieures: number, depensesAnterieures: number }): Observable<Logement> {
    return this.http.post<Logement>(`${this.url}/${id}/initialiser`, data);
  }
}
