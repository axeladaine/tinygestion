import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';
import { registerLocaleData } from '@angular/common';
import localeFr from '@angular/common/locales/fr';
import flatpickr from 'flatpickr';
import { French } from 'flatpickr/dist/l10n/fr.js';

registerLocaleData(localeFr, 'fr');
flatpickr.localize(French);

bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error(err));


