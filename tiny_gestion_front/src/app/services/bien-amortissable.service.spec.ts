import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { BienAmortissableService } from './bien-amortissable.service';
import { BienAmortissable } from '../models/bien-amortissable.model';

describe('BienAmortissableService', () => {
  let service: BienAmortissableService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [BienAmortissableService]
    });
    service = TestBed.inject(BienAmortissableService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should fetch assets from API', () => {
    const mockBiens: BienAmortissable[] = [
      { id: 300, nom: 'Terrasse', montantTtc: 3000 } as any as BienAmortissable
    ];

    service.getBiens(10).subscribe(data => {
      expect(data).toEqual(mockBiens);
    });

    const req = httpMock.expectOne('/api/biens-amortissables/logement/10');
    expect(req.request.method).toBe('GET');
    req.flush(mockBiens);
  });

  it('should save asset via POST', () => {
    const newBien: BienAmortissable = { nom: 'Terrasse', montantTtc: 3000 } as any as BienAmortissable;

    service.saveBien(newBien).subscribe(data => {
      expect(data).toEqual(newBien);
    });

    const req = httpMock.expectOne('/api/biens-amortissables');
    expect(req.request.method).toBe('POST');
    req.flush(newBien);
  });
});
