import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AdministrationService } from './administration.service';
import { JournalSecurite } from '../models/journal-securite.model';

describe('AdministrationService', () => {
  let service: AdministrationService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AdministrationService]
    });
    service = TestBed.inject(AdministrationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should fetch JournalSecurite list', () => {
    const mockLogs: JournalSecurite[] = [
      {
        id: 1,
        email: 'test@example.com',
        typeEvenement: 'CONNEXION_REUSSIE',
        adresseIp: '127.0.0.1',
        details: 'Ok',
        dateEvenement: '2026-06-21T12:00:00'
      }
    ];

    service.getJournalSecurite().subscribe(logs => {
      expect(logs).toEqual(mockLogs);
    });

    const req = httpMock.expectOne('/api/administration/journal-securite');
    expect(req.request.method).toBe('GET');
    req.flush(mockLogs);
  });

  it('should create an assistant account', () => {
    const mockAssistant = { email: 'assistant@example.com', motDePasse: 'pass', prenom: 'A', nom: 'B' };
    const mockResponse = { id: 10, email: 'assistant@example.com' };

    service.creerAssistant(mockAssistant).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne('/api/administration/assistant');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockAssistant);
    req.flush(mockResponse);
  });

  it('should fetch assistants list', () => {
    const mockAssistants = [{ id: 10, email: 'assistant@example.com' }];

    service.getAssistants().subscribe(assistants => {
      expect(assistants).toEqual(mockAssistants);
    });

    const req = httpMock.expectOne('/api/administration/assistant');
    expect(req.request.method).toBe('GET');
    req.flush(mockAssistants);
  });

  it('should delete assistant', () => {
    service.supprimerAssistant(10).subscribe();

    const req = httpMock.expectOne('/api/administration/assistant/10');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
