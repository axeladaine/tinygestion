import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Depense } from '../models/depense.model';

@Injectable({
  providedIn: 'root'
})
export class DepenseService {
  private http = inject(HttpClient);
  private url = '/api/depenses';

  getDepenses(logementId: number): Observable<Depense[]> {
    return this.http.get<Depense[]>(`${this.url}/logement/${logementId}`);
  }

  getDepensesByPeriod(logementId: number, debut: string, fin: string): Observable<Depense[]> {
    return this.http.get<Depense[]>(`${this.url}/logement/${logementId}/periode?debut=${debut}&fin=${fin}`);
  }

  saveDepense(depense: Depense): Observable<Depense> {
    return this.http.post<Depense>(this.url, depense);
  }

  updateDepense(id: number, depense: Depense): Observable<Depense> {
    return this.http.put<Depense>(`${this.url}/${id}`, depense);
  }

  deleteDepense(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
