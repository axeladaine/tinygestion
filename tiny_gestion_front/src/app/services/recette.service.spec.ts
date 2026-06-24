import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RecetteService } from './recette.service';
import { Recette } from '../models/recette.model';

describe('RecetteService', () => {
  let service: RecetteService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [RecetteService]
    });
    service = TestBed.inject(RecetteService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should fetch recipes from API', () => {
    const mockRecettes: Recette[] = [
      { id: 100, plateforme: 'AIRBNB', montantBrut: 300 } as any as Recette
    ];

    service.getRecettes(10).subscribe(data => {
      expect(data).toEqual(mockRecettes);
    });

    const req = httpMock.expectOne('/api/recettes/logement/10');
    expect(req.request.method).toBe('GET');
    req.flush(mockRecettes);
  });

  it('should save recipe via POST', () => {
    const newRecette: Recette = { plateforme: 'AIRBNB', montantBrut: 300 } as any as Recette;

    service.saveRecette(newRecette).subscribe(data => {
      expect(data).toEqual(newRecette);
    });

    const req = httpMock.expectOne('/api/recettes');
    expect(req.request.method).toBe('POST');
    req.flush(newRecette);
  });

  it('should fetch recipes by period via GET', () => {
    const mockRecettes: Recette[] = [
      { id: 100, plateforme: 'AIRBNB', montantBrut: 300 } as any as Recette
    ];

    service.getRecettesByPeriod(10, '2026-01-01', '2026-12-31').subscribe(data => {
      expect(data).toEqual(mockRecettes);
    });

    const req = httpMock.expectOne('/api/recettes/logement/10/periode?debut=2026-01-01&fin=2026-12-31');
    expect(req.request.method).toBe('GET');
    req.flush(mockRecettes);
  });

  it('should update recipe via PUT', () => {
    const updatedRecette: Recette = { id: 100, plateforme: 'BOOKING', montantBrut: 400 } as any as Recette;

    service.updateRecette(100, updatedRecette).subscribe(data => {
      expect(data).toEqual(updatedRecette);
    });

    const req = httpMock.expectOne('/api/recettes/100');
    expect(req.request.method).toBe('PUT');
    req.flush(updatedRecette);
  });

  it('should delete recipe via DELETE', () => {
    service.deleteRecette(100).subscribe();

    const req = httpMock.expectOne('/api/recettes/100');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
