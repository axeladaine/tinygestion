import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { BilanFiscalService } from './bilan-fiscal.service';
import { BilanFiscal } from '../models/bilan-fiscal.model';

describe('BilanFiscalService', () => {
  let service: BilanFiscalService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [BilanFiscalService]
    });
    service = TestBed.inject(BilanFiscalService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should fetch BilanFiscal from API', () => {
    const mockBilan: BilanFiscal = {
      logementId: 10,
      annee: 2026,
      recettesBrutes: 3000,
      depensesRetenues: 1200,
      amortissementDisponible: 1000,
      resultatAvantAmortissement: 1800,
      amortissementUtilise: 1000,
      resultatReelImposable: 800,
      amortissementNonUtilise: 0,
      estMeubleTourisme: false,
      abattementMicroBic: 1500,
      resultatMicroBicImposable: 1500,
      regimeFiscalConseille: 'REGIME_REEL_AVANTAGEUX',
      differenceGainImposable: 700,
      estLoueCourteDuree: false,
      caseFiscaleMicroBic: '5NI'
    };

    service.getBilanFiscal(10, 2026).subscribe(data => {
      expect(data).toEqual(mockBilan);
    });

    const req = httpMock.expectOne('/api/bilan-fiscal/logement/10/annee/2026');
    expect(req.request.method).toBe('GET');
    req.flush(mockBilan);
  });
});
