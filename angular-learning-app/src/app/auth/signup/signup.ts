import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-signup',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './signup.html',
  styleUrl: './signup.css'
})
export class Signup {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  isLoading = signal(false);
  errorMessage = signal('');

  signupForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  get nameControl() {
    return this.signupForm.controls.name;
  }

  get emailControl() {
    return this.signupForm.controls.email;
  }

  get passwordControl() {
    return this.signupForm.controls.password;
  }

  signup() {
    this.errorMessage.set('');

    if (this.signupForm.invalid) {
      this.signupForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);

    const request = this.signupForm.getRawValue();

    this.authService.signup(request).subscribe({
      next: response => {
        console.log('Signup successful:', response);

        this.isLoading.set(false);
        this.router.navigate(['/skills']);
      },
      error: error => {
        this.handleSignupError(error);
        this.isLoading.set(false);
      }
    });
  }

  private handleSignupError(error: HttpErrorResponse) {
    if (error.status === 0) {
      this.errorMessage.set('Backend is not reachable.');
      return;
    }

    if (error.status === 400) {
      this.errorMessage.set(error.error?.message || 'Invalid signup data.');
      return;
    }

    this.errorMessage.set(error.error?.message || 'Signup failed.');
  }
}