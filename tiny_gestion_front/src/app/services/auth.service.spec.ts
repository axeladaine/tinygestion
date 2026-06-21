import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService, Utilisateur, AuthResponse } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const mockUser: Utilisateur = {
    id: '10',
    nom: 'Dupont',
    prenom: 'Jean',
    email: 'jean@example.com',
    role: 'PROPRIETAIRE'
  };

  const mockResponse: AuthResponse = {
    token: 'fake-jwt-token',
    utilisateur: mockUser
  };

  beforeEach(() => {
    // Nettoyer le localStorage avant chaque test
    localStorage.clear();

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created and restore session from localStorage if present', () => {
    localStorage.setItem('jwt_token', 'stored-token');
    localStorage.setItem('utilisateur_session', JSON.stringify(mockUser));

    const newService = TestBed.inject(AuthService);
    expect(newService.isAuthenticated()).toBeTrue();
    expect(newService.currentUser()).toEqual(mockUser);
    expect(newService.getToken()).toBe('stored-token');
  });

  it('should login and save session to localStorage', () => {
    service = TestBed.inject(AuthService);
    service.connexion('jean@example.com', 'password123').subscribe(response => {
      expect(response).toEqual(mockResponse);
      expect(service.isAuthenticated()).toBeTrue();
      expect(service.currentUser()).toEqual(mockUser);
      expect(localStorage.getItem('jwt_token')).toBe('fake-jwt-token');
      expect(localStorage.getItem('utilisateur_session')).toBe(JSON.stringify(mockUser));
    });

    const req = httpMock.expectOne('/api/authentification/connexion');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'jean@example.com', motDePasse: 'password123' });
    req.flush(mockResponse);
  });

  it('should register and save session to localStorage', () => {
    service = TestBed.inject(AuthService);
    const payload = {
      email: 'jean@example.com',
      motDePasse: 'password123',
      prenom: 'Jean',
      nom: 'Dupont',
      nomLogement: 'Ma Tiny Cabane'
    };

    service.inscription(payload).subscribe(response => {
      expect(response).toEqual(mockResponse);
      expect(service.isAuthenticated()).toBeTrue();
      expect(service.currentUser()).toEqual(mockUser);
      expect(localStorage.getItem('jwt_token')).toBe('fake-jwt-token');
    });

    const req = httpMock.expectOne('/api/authentification/inscription');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush(mockResponse);
  });

  it('should clear session on deconnexion', () => {
    service = TestBed.inject(AuthService);
    localStorage.setItem('jwt_token', 'stored-token');
    localStorage.setItem('utilisateur_session', JSON.stringify(mockUser));

    // Force restauration
    service.connexion('jean@example.com', 'password123').subscribe();
    const req = httpMock.expectOne('/api/authentification/connexion');
    req.flush(mockResponse);

    expect(service.isAuthenticated()).toBeTrue();

    service.deconnexion();

    expect(service.isAuthenticated()).toBeFalse();
    expect(service.currentUser()).toBeNull();
    expect(localStorage.getItem('jwt_token')).toBeNull();
    expect(localStorage.getItem('utilisateur_session')).toBeNull();
  });

  it('should handle corrupt json in localStorage when restoring session', () => {
    localStorage.setItem('jwt_token', 'stored-token');
    localStorage.setItem('utilisateur_session', 'invalid-json-{');

    const newService = TestBed.inject(AuthService);
    expect(newService.isAuthenticated()).toBeFalse();
    expect(newService.currentUser()).toBeNull();
    expect(localStorage.getItem('jwt_token')).toBeNull();
  });
});
