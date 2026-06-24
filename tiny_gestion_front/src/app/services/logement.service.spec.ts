import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { LogementService } from './logement.service';
import { Logement } from '../models/logement.model';

describe('LogementService', () => {
  let service: LogementService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [LogementService]
    });
    service = TestBed.inject(LogementService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should fetch lodgings from API', () => {
    const mockLogements: Logement[] = [
      { id: 10, nom: 'Tiny du Lac' } as any as Logement
    ];

    service.getLogements().subscribe(data => {
      expect(data).toEqual(mockLogements);
    });

    const req = httpMock.expectOne('/api/logements');
    expect(req.request.method).toBe('GET');
    req.flush(mockLogements);
  });

  it('should save lodging via POST', () => {
    const newLogement: Logement = { nom: 'Tiny du Lac' } as any as Logement;

    service.saveLogement(newLogement).subscribe(data => {
      expect(data).toEqual(newLogement);
    });

    const req = httpMock.expectOne('/api/logements');
    expect(req.request.method).toBe('POST');
    req.flush(newLogement);
  });

  it('should fetch lodging by ID via GET', () => {
    const mockLogement: Logement = { id: 10, nom: 'Tiny du Lac' } as any as Logement;

    service.getLogementById(10).subscribe(data => {
      expect(data).toEqual(mockLogement);
    });

    const req = httpMock.expectOne('/api/logements/10');
    expect(req.request.method).toBe('GET');
    req.flush(mockLogement);
  });

  it('should update lodging via PUT', () => {
    const updatedLogement: Logement = { id: 10, nom: 'Tiny du Lac Nouveau' } as any as Logement;

    service.updateLogement(10, updatedLogement).subscribe(data => {
      expect(data).toEqual(updatedLogement);
    });

    const req = httpMock.expectOne('/api/logements/10');
    expect(req.request.method).toBe('PUT');
    req.flush(updatedLogement);
  });

  it('should initialize lodging via POST to initialiser endpoint', () => {
    const mockLogement: Logement = { id: 10, nom: 'Tiny du Lac', initialise: true } as any as Logement;
    const initData = { recettesAnterieures: 4500, depensesAnterieures: 850 };

    service.initialiserLogement(10, initData).subscribe(data => {
      expect(data).toEqual(mockLogement);
    });

    const req = httpMock.expectOne('/api/logements/10/initialiser');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(initData);
    req.flush(mockLogement);
  });
});
