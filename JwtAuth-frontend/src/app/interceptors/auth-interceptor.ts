import { HttpClient, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const http = inject(HttpClient);
    const router = inject(Router);

    const accessToken =
      localStorage.getItem('accessToken');

    const refreshToken =
      localStorage.getItem('refreshToken');

    // Do not attach JWT to login/refresh requests
    const isAuthRequest =
      req.url.includes('/api/auth/login') ||
      req.url.includes('/api/auth/refresh') ||
  req.url.includes('/api/auth/logout');

    // -----------------------------
    // 1. Attach access token
    // -----------------------------

    if (accessToken && !isAuthRequest) {

      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${accessToken}`
        }
      });
    }

    // -----------------------------
    // 2. Send request
    // -----------------------------

    return next(req).pipe(

      catchError(error => {

        // -----------------------------
        // 3. Access token expired
        // -----------------------------

        if (
          error.status === 401 &&
          refreshToken &&
          !isAuthRequest
        ) {

          return http.post<any>(
            'http://localhost:8080/api/auth/refresh',
            {
              refreshToken: refreshToken
            }
          ).pipe(

            switchMap(response => {

              // Save new access token
              localStorage.setItem(
                'accessToken',
                response.token
              );

              // Refresh token may remain same
              if (response.refreshToken) {
                localStorage.setItem(
                  'refreshToken',
                  response.refreshToken
                );
              }

              // -----------------------------
              // 4. Retry original request
              // -----------------------------

              const retryRequest =
                req.clone({
                  setHeaders: {
                    Authorization:
                      `Bearer ${response.token}`
                  }
                });

              return next(retryRequest);
            }),

            catchError(refreshError => {

              // Refresh token invalid/expired
              localStorage.clear();

              router.navigate(['/login']);

              return throwError(
                () => refreshError
              );
            })
          );
        }

        return throwError(() => error);
      })
    );
  };