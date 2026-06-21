import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { DocumentJustificatifService } from './document-justificatif.service';
import { DocumentJustificatif } from '../models/document-justificatif.model';

describe('DocumentJustificatifService', () => {
  let service: DocumentJustificatifService;
  let httpMock: HttpTestingController;

  const mockDocs: DocumentJustificatif[] = [
    {
      id: 1,
      nomFichier: 'test.pdf',
      typeFichier: 'application/pdf',
      cheminStockage: 'storage/test.pdf',
      typeDocument: 'FACTURE',
      dateTeleversement: '2026-06-21',
      logement: { id: 10 }
    }
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [DocumentJustificatifService]
    });
    service = TestBed.inject(DocumentJustificatifService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should fetch documents by logementId', () => {
    service.getDocuments(10).subscribe(docs => {
      expect(docs).toEqual(mockDocs);
    });

    const req = httpMock.expectOne('/api/documents-justificatifs/logement/10');
    expect(req.request.method).toBe('GET');
    req.flush(mockDocs);
  });

  it('should upload a file', () => {
    const mockFile = new File(['file content'], 'test.pdf', { type: 'application/pdf' });
    const mockResponse: DocumentJustificatif = mockDocs[0];

    service.televerser(mockFile, 10, 'FACTURE', 'DEPENSE', 5).subscribe(doc => {
      expect(doc).toEqual(mockResponse);
    });

    const req = httpMock.expectOne('/api/documents-justificatifs/televerser');
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBeTrue();
    req.flush(mockResponse);
  });

  it('should upload a file without optional parameters', () => {
    const mockFile = new File(['file content'], 'test.pdf', { type: 'application/pdf' });
    const mockResponse: DocumentJustificatif = mockDocs[0];

    service.televerser(mockFile, 10, 'FACTURE').subscribe(doc => {
      expect(doc).toEqual(mockResponse);
    });

    const req = httpMock.expectOne('/api/documents-justificatifs/televerser');
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('should download a file as Blob', () => {
    const mockBlob = new Blob(['blob content'], { type: 'application/pdf' });

    service.telecharger(1).subscribe(blob => {
      expect(blob).toEqual(mockBlob);
    });

    const req = httpMock.expectOne('/api/documents-justificatifs/1/telecharger');
    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('blob');
    req.flush(mockBlob);
  });

  it('should delete a document', () => {
    service.deleteDocument(1).subscribe();

    const req = httpMock.expectOne('/api/documents-justificatifs/1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
