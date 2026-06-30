import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  isLoading = signal(false);
  errorMessage = signal('');

  loginForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });

  get emailControl() {
    return this.loginForm.controls.email;
  }

  get passwordControl() {
    return this.loginForm.controls.password;
  }

  login() {
    this.errorMessage.set('');

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);

    const request = this.loginForm.getRawValue();

    this.authService.login(request).subscribe({
      next: response => {
        console.log('Login successful:', response);

        this.isLoading.set(false);
        this.router.navigate(['/skills']);
      },
      error: error => {
        this.handleLoginError(error);
        this.isLoading.set(false);
      }
    });
  }

  private handleLoginError(error: HttpErrorResponse) {
    if (error.status === 0) {
      this.errorMessage.set('Backend is not reachable.');
      return;
    }

    if (error.status === 401 || error.status === 403) {
      this.errorMessage.set('Invalid email or password.');
      return;
    }

    this.errorMessage.set(error.error?.message || 'Login failed.');
  }
}