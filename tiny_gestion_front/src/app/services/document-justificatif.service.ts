import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DocumentJustificatif } from '../models/document-justificatif.model';

@Injectable({
  providedIn: 'root'
})
export class DocumentJustificatifService {
  private http = inject(HttpClient);
  private url = '/api/documents-justificatifs';

  getDocuments(logementId: number): Observable<DocumentJustificatif[]> {
    return this.http.get<DocumentJustificatif[]>(`${this.url}/logement/${logementId}`);
  }

  televerser(
    file: File,
    logementId: number,
    typeDocument: string,
    entiteLieeType?: string,
    entiteLieeId?: number
  ): Observable<DocumentJustificatif> {
    const formData = new FormData();
    formData.append('fichier', file);
    formData.append('logementId', logementId.toString());
    formData.append('typeDocument', typeDocument);
    if (entiteLieeType) {
      formData.append('entiteLieeType', entiteLieeType);
    }
    if (entiteLieeId) {
      formData.append('entiteLieeId', entiteLieeId.toString());
    }

    return this.http.post<DocumentJustificatif>(`${this.url}/televerser`, formData);
  }

  telecharger(id: number): Observable<Blob> {
    return this.http.get(`${this.url}/${id}/telecharger`, { responseType: 'blob' });
  }

  deleteDocument(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
