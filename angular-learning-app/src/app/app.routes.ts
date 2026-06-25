import { Routes } from '@angular/router';
import { HomePage } from './pages/home-page/home-page';
import { ProfilePage } from './pages/profile-page/profile-page';
import { SkillsPage } from './pages/skills-page/skills-page';
import { AboutPage } from './pages/about-page/about-page';
import { NotFound } from './pages/not-found/not-found';
import {SkillDetailPage} from './pages/skill-detail-page/skill-detail-page';
import { Signup } from './auth/signup/signup';
import { Login } from './auth/login/login';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'home',
    pathMatch: 'full'
  },  
  {
    path: 'home',
    component: HomePage
  },
  {
    path: 'profile',
    component: ProfilePage,
    canActivate: [authGuard]
  },
  {
    path: 'skills',
    component: SkillsPage,
    canActivate: [authGuard]
  },
  {
    path: 'skills/:id',
    component: SkillDetailPage,
    canActivate: [authGuard]
  },
    {
    path: 'login',
    component: Login
  },
  {
    path: 'signup',
    component: Signup
  },
  {
    path: 'about',
    component: AboutPage,
    canActivate: [authGuard]
  },
  {
    path: '**',
    component: NotFound
  }
];