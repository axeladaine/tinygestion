import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BilanFiscal } from '../models/bilan-fiscal.model';

@Injectable({
  providedIn: 'root'
})
export class BilanFiscalService {
  private http = inject(HttpClient);
  private url = '/api/bilan-fiscal';

  getBilanFiscal(logementId: number, annee: number): Observable<BilanFiscal> {
    return this.http.get<BilanFiscal>(`${this.url}/logement/${logementId}/annee/${annee}`);
  }

  telechargerDeclarationPdf(logementId: number, annee: number): Observable<Blob> {
    return this.http.get(`${this.url}/logement/${logementId}/annee/${annee}/export-pdf`, {
      responseType: 'blob'
    });
  }
}
