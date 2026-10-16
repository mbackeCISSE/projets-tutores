import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { routes } from './app.routes';
import { utilisateurInterceptor } from './services/api.service';
import { MOCK_ENABLED } from './services/app-settings';
import { mockApiInterceptor } from './services/mock-api.service';
export const appConfig: ApplicationConfig = {
  providers: [provideZoneChangeDetection({ eventCoalescing: true }), provideRouter(routes),
    { provide: MOCK_ENABLED, useValue: false },
    provideHttpClient(withInterceptors([utilisateurInterceptor, mockApiInterceptor]))]
};
