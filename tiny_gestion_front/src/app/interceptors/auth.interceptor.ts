import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  let authReq = req;

  // Ajouter l'en-tête Authorization avec le token JWT si disponible
  if (token) {
    authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(authReq).pipe(
    catchError((error: any) => {
      if (error instanceof HttpErrorResponse) {
        // En cas de 403 (Forbidden) ou 401 (Unauthorized)
        if (error.status === 403 || error.status === 401) {
          authService.deconnexion();
        }
      }
      return throwError(() => error);
    })
  );
};
