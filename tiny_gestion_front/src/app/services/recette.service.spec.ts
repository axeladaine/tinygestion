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
});
