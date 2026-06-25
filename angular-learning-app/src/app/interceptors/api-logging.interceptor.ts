
import { catchError, tap, throwError } from 'rxjs';
import { HttpInterceptorFn, HttpResponse } from '@angular/common/http';

export const apiLoggingInterceptor: HttpInterceptorFn = (request, next) => {
  const modifiedRequest = request.clone({
    setHeaders: {
      'X-Client-Name': 'Angular Skill Dashboard'
    }
  });

  console.log('API Request:', modifiedRequest.method, modifiedRequest.urlWithParams);

  const startedAt = Date.now();

  return next(modifiedRequest).pipe(
    tap((event) => {
      if (event instanceof HttpResponse) {
        const duration = Date.now() - startedAt;

        console.log('API Response received for:', modifiedRequest.urlWithParams);
        console.log('API Duration:', duration + 'ms');
      }
    }),
    catchError(error => {
      const duration = Date.now() - startedAt;

      console.error('API Error:', modifiedRequest.method, modifiedRequest.urlWithParams);
      console.error('Status:', error.status);
      console.error('Duration:', duration + 'ms');

      return throwError(() => error);
    })
  );
};