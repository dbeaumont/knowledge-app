import { Routes } from '@angular/router';
import { ChatPageComponent } from './chat/chat-page.component';
import { DocumentsPageComponent } from './documents/documents-page.component';
import { ProfilePageComponent } from './auth/profile-page.component';

export const routes: Routes = [
  { path: 'chat', component: ChatPageComponent },
  { path: 'documents', component: DocumentsPageComponent },
  { path: 'profile', component: ProfilePageComponent },
  { path: '', pathMatch: 'full', redirectTo: 'chat' }
];
