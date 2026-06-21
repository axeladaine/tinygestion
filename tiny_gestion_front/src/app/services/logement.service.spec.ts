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
});
