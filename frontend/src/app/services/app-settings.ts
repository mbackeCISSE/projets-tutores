import { InjectionToken } from '@angular/core';

export const MOCK_ENABLED = new InjectionToken<boolean>('MOCK_ENABLED', { factory: () => false });
