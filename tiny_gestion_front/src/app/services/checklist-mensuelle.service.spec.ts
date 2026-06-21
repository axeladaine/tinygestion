import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ChecklistMensuelleService } from './checklist-mensuelle.service';
import { ChecklistMensuelle } from '../models/checklist-mensuelle.model';

describe('ChecklistMensuelleService', () => {
  let service: ChecklistMensuelleService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ChecklistMensuelleService]
    });
    service = TestBed.inject(ChecklistMensuelleService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should fetch checklist from API', () => {
    const mockChecklist: ChecklistMensuelle = {
      id: 100,
      annee: 2026,
      mois: 6,
      tachesJson: '[]',
      tauxCompletion: 0
    };

    service.getOrCreateChecklist(10, 2026, 6).subscribe(data => {
      expect(data).toEqual(mockChecklist);
    });

    const req = httpMock.expectOne('/api/checklists-mensuelles/logement/10/annee/2026/mois/6');
    expect(req.request.method).toBe('GET');
    req.flush(mockChecklist);
  });

  it('should update checklist tasks', () => {
    const mockChecklist: ChecklistMensuelle = {
      id: 100,
      annee: 2026,
      mois: 6,
      tachesJson: '[]',
      tauxCompletion: 0
    };

    service.updateChecklist(100, '[]').subscribe(data => {
      expect(data).toEqual(mockChecklist);
    });

    const req = httpMock.expectOne('/api/checklists-mensuelles/100');
    expect(req.request.method).toBe('PUT');
    expect(req.request.headers.get('Content-Type')).toBe('text/plain');
    req.flush(mockChecklist);
  });
});
