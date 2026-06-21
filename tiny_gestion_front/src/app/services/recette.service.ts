import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Recette } from '../models/recette.model';

@Injectable({
  providedIn: 'root'
})
export class RecetteService {
  private http = inject(HttpClient);
  private url = '/api/recettes';

  getRecettes(logementId: number): Observable<Recette[]> {
    return this.http.get<Recette[]>(`${this.url}/logement/${logementId}`);
  }

  getRecettesByPeriod(logementId: number, debut: string, fin: string): Observable<Recette[]> {
    return this.http.get<Recette[]>(`${this.url}/logement/${logementId}/periode?debut=${debut}&fin=${fin}`);
  }

  saveRecette(recette: Recette): Observable<Recette> {
    return this.http.post<Recette>(this.url, recette);
  }

  updateRecette(id: number, recette: Recette): Observable<Recette> {
    return this.http.put<Recette>(`${this.url}/${id}`, recette);
  }

  deleteRecette(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
