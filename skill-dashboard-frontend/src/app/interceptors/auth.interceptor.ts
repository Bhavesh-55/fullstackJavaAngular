import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';

import { environment } from '../../environments/environment';
import { AuthService } from '../services/auth';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const isApiRequest = request.url.startsWith(environment.apiBaseUrl);
  const isAuthRequest = request.url.includes('/auth/');

  const accessToken = authService.getAccessToken();

  let authRequest = request;

  if (accessToken && isApiRequest && !isAuthRequest) {
    authRequest = request.clone({
      setHeaders: {
        Authorization: `Bearer ${accessToken}`
      }
    });
  }

  return next(authRequest).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && isApiRequest && !isAuthRequest) {
        const refreshToken = authService.getRefreshToken();

        if (!refreshToken) {
          authService.clearAuthData();
          router.navigate(['/login']);
          return throwError(() => error);
        }

        return authService.refreshAccessToken().pipe(
          switchMap(response => {
            const retryRequest = request.clone({
              setHeaders: {
                Authorization: `Bearer ${response.accessToken}`
              }
            });

            return next(retryRequest);
          }),
          catchError(refreshError => {
            authService.clearAuthData();
            router.navigate(['/login']);

            return throwError(() => refreshError);
          })
        );
      }

      return throwError(() => error);
    })
  );
};