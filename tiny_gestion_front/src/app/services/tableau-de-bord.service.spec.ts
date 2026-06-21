import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TableauDeBordService, StatsMensuelles } from './tableau-de-bord.service';

describe('TableauDeBordService', () => {
  let service: TableauDeBordService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TableauDeBordService]
    });
    service = TestBed.inject(TableauDeBordService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should fetch StatsMensuelles from API', () => {
    const mockStats: StatsMensuelles = {
      totalRecettes: 1500,
      totalDepenses: 600,
      resultatEstime: 900,
      justificatifsManquants: 3
    };

    service.getStatsMensuelles(10, 6, 2026).subscribe(stats => {
      expect(stats).toEqual(mockStats);
    });

    const req = httpMock.expectOne('/api/tableau-de-bord/logement/10?mois=6&annee=2026');
    expect(req.request.method).toBe('GET');
    req.flush(mockStats);
  });
});
