import { Routes } from '@angular/router';
import { Dashboard } from './components/dashboard/dashboard';
import { Login } from './components/login/login';
import { authGuard } from './guards/auth-guard';
import { Oauth2Callback } from './components/oauth2-callback/oauth2-callback';

export const routes: Routes = [
     {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },

  {
    path: 'login',
    component: Login
  },
  {
    path: 'oauth2/callback',
    component: Oauth2Callback
  },

  {
    path: 'dashboard',
    component: Dashboard,
    canActivate: [authGuard]
  }
];
