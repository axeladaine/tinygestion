import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BienAmortissable } from '../models/bien-amortissable.model';

@Injectable({
  providedIn: 'root'
})
export class BienAmortissableService {
  private http = inject(HttpClient);
  private url = '/api/biens-amortissables';

  getBiens(logementId: number): Observable<BienAmortissable[]> {
    return this.http.get<BienAmortissable[]>(`${this.url}/logement/${logementId}`);
  }

  saveBien(bien: BienAmortissable): Observable<BienAmortissable> {
    return this.http.post<BienAmortissable>(this.url, bien);
  }

  updateBien(id: number, bien: BienAmortissable): Observable<BienAmortissable> {
    return this.http.put<BienAmortissable>(`${this.url}/${id}`, bien);
  }

  deleteBien(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
