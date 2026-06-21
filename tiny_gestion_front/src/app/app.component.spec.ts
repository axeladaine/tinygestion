import { TestBed } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { AuthService } from './services/auth.service';
import { Router, ActivatedRoute } from '@angular/router';
import { signal } from '@angular/core';

describe('AppComponent', () => {
  let authSpy: any;
  let routerSpy: any;

  beforeEach(async () => {
    authSpy = jasmine.createSpyObj('AuthService', ['deconnexion']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    // Mock computed signals
    authSpy.isAuthenticated = signal(false);
    authSpy.currentUser = signal(null);

    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        { provide: AuthService, useValue: authSpy },
        { provide: Router, useValue: routerSpy },
        { provide: ActivatedRoute, useValue: {} }
      ]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should call deconnexion and navigate to /connexion', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;

    app.deconnexion();

    expect(authSpy.deconnexion).toHaveBeenCalled();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/connexion']);
  });
});
