import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ChecklistMensuelle } from '../models/checklist-mensuelle.model';

@Injectable({
  providedIn: 'root'
})
export class ChecklistMensuelleService {
  private http = inject(HttpClient);
  private url = '/api/checklists-mensuelles';

  getOrCreateChecklist(logementId: number, annee: number, mois: number): Observable<ChecklistMensuelle> {
    return this.http.get<ChecklistMensuelle>(`${this.url}/logement/${logementId}/annee/${annee}/mois/${mois}`);
  }

  updateChecklist(id: number, newTachesJson: string): Observable<ChecklistMensuelle> {
    // Le controller attend un RequestBody String contenant le JSON brut
    return this.http.put<ChecklistMensuelle>(`${this.url}/${id}`, newTachesJson, {
      headers: { 'Content-Type': 'text/plain' }
    });
  }

  getChecklists(logementId: number, annee: number): Observable<ChecklistMensuelle[]> {
    return this.http.get<ChecklistMensuelle[]>(`${this.url}/logement/${logementId}/annee/${annee}`);
  }
}
