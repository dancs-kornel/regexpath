import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';
import 'prismjs';
import 'prismjs/components/prism-markup';

bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error(err));
