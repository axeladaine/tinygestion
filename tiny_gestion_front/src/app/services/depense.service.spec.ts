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

  it('should fetch expenses by period via GET', () => {
    const mockDepenses: Depense[] = [
      { id: 200, fournisseur: 'EDF', montantTtc: 120 } as any as Depense
    ];

    service.getDepensesByPeriod(10, '2026-01-01', '2026-12-31').subscribe(data => {
      expect(data).toEqual(mockDepenses);
    });

    const req = httpMock.expectOne('/api/depenses/logement/10/periode?debut=2026-01-01&fin=2026-12-31');
    expect(req.request.method).toBe('GET');
    req.flush(mockDepenses);
  });

  it('should update expense via PUT', () => {
    const updatedDepense: Depense = { id: 200, fournisseur: 'EDF Nouveau', montantTtc: 150 } as any as Depense;

    service.updateDepense(200, updatedDepense).subscribe(data => {
      expect(data).toEqual(updatedDepense);
    });

    const req = httpMock.expectOne('/api/depenses/200');
    expect(req.request.method).toBe('PUT');
    req.flush(updatedDepense);
  });

  it('should delete expense via DELETE', () => {
    service.deleteDepense(200).subscribe();

    const req = httpMock.expectOne('/api/depenses/200');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
