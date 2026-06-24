import { TestBed } from '@angular/core/testing';
import { HttpClient, HttpErrorResponse, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from '../services/auth.service';

describe('authInterceptor', () => {
  let httpMock: HttpTestingController;
  let httpClient: HttpClient;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    const authSpy = jasmine.createSpyObj('AuthService', ['getToken', 'deconnexion']);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authSpy },
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting()
      ]
    });

    httpMock = TestBed.inject(HttpTestingController);
    httpClient = TestBed.inject(HttpClient);
    authServiceSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should add Authorization header if token is present', () => {
    authServiceSpy.getToken.and.returnValue('my-jwt-token');

    httpClient.get('/api/test').subscribe();

    const req = httpMock.expectOne('/api/test');
    expect(req.request.headers.has('Authorization')).toBeTrue();
    expect(req.request.headers.get('Authorization')).toBe('Bearer my-jwt-token');
  });

  it('should not add Authorization header if token is absent', () => {
    authServiceSpy.getToken.and.returnValue(null);

    httpClient.get('/api/test').subscribe();

    const req = httpMock.expectOne('/api/test');
    expect(req.request.headers.has('Authorization')).toBeFalse();
  });

  it('should trigger deconnexion on 401 Unauthorized', () => {
    authServiceSpy.getToken.and.returnValue('token');

    httpClient.get('/api/test').subscribe({
      next: () => fail('should have failed'),
      error: (err: HttpErrorResponse) => {
        expect(err.status).toBe(401);
        expect(authServiceSpy.deconnexion).toHaveBeenCalled();
      }
    });

    const req = httpMock.expectOne('/api/test');
    req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });
  });

  it('should trigger deconnexion on 403 Forbidden', () => {
    authServiceSpy.getToken.and.returnValue('token');

    httpClient.get('/api/test').subscribe({
      next: () => fail('should have failed'),
      error: (err: HttpErrorResponse) => {
        expect(err.status).toBe(403);
        expect(authServiceSpy.deconnexion).toHaveBeenCalled();
      }
    });

    const req = httpMock.expectOne('/api/test');
    req.flush('Forbidden', { status: 403, statusText: 'Forbidden' });
  });

  it('should not trigger deconnexion on other errors', () => {
    authServiceSpy.getToken.and.returnValue('token');

    httpClient.get('/api/test').subscribe({
      next: () => fail('should have failed'),
      error: (err: HttpErrorResponse) => {
        expect(err.status).toBe(500);
        expect(authServiceSpy.deconnexion).not.toHaveBeenCalled();
      }
    });

    const req = httpMock.expectOne('/api/test');
    req.flush('Internal Server Error', { status: 500, statusText: 'Server Error' });
  });
});
