import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { DepenseService } from './depense.service';
import { Depense } from '../models/depense.model';

describe('DepenseService', () => {
  let service: DepenseService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [DepenseService]
    });
    service = TestBed.inject(DepenseService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should fetch expenses from API', () => {
    const mockDepenses: Depense[] = [
      { id: 200, fournisseur: 'EDF', montantTtc: 120 } as any as Depense
    ];

    service.getDepenses(10).subscribe(data => {
      expect(data).toEqual(mockDepenses);
    });

    const req = httpMock.expectOne('/api/depenses/logement/10');
    expect(req.request.method).toBe('GET');
    req.flush(mockDepenses);
  });

  it('should save expense via POST', () => {
    const newDepense: Depense = { fournisseur: 'EDF', montantTtc: 120 } as any as Depense;

    service.saveDepense(newDepense).subscribe(data => {
      expect(data).toEqual(newDepense);
    });

    const req = httpMock.expectOne('/api/depenses');
    expect(req.request.method).toBe('POST');
    req.flush(newDepense);
  });
});
