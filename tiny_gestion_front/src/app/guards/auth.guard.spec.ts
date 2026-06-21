import { TestBed } from '@angular/core/testing';
import { Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { authGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';

describe('authGuard', () => {
  let mockAuthService: { isAuthenticated: jasmine.Spy };
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(() => {
    mockAuthService = {
      isAuthenticated: jasmine.createSpy('isAuthenticated')
    };
    const rotSpy = jasmine.createSpyObj('Router', ['createUrlTree']);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: rotSpy }
      ]
    });

    routerSpy = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  it('should return true if user is authenticated', () => {
    mockAuthService.isAuthenticated.and.returnValue(true);

    const result = TestBed.runInInjectionContext(() => 
      authGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot)
    );

    expect(result).toBeTrue();
  });

  it('should redirect to /connexion if user is not authenticated', () => {
    mockAuthService.isAuthenticated.and.returnValue(false);
    const mockUrlTree = {};
    routerSpy.createUrlTree.and.returnValue(mockUrlTree as any);

    const result = TestBed.runInInjectionContext(() => 
      authGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot)
    );

    expect(result).toBe(mockUrlTree as any);
    expect(routerSpy.createUrlTree).toHaveBeenCalledWith(['/connexion']);
  });
});
