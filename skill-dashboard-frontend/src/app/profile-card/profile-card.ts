import { Component, computed, inject } from '@angular/core';

import { AuthService } from '../services/auth';

@Component({
  selector: 'app-profile-card',
  imports: [],
  templateUrl: './profile-card.html',
  styleUrl: './profile-card.css',
})
export class ProfileCard {
  private authService = inject(AuthService);

  currentUser = this.authService.currentUser;

  displayName = computed(() => this.currentUser()?.name || 'User');
  displayEmail = computed(() => this.currentUser()?.email || '-');
  displayRole = computed(() => this.currentUser()?.role || '-');
  displayUserId = computed(() => this.currentUser()?.userId || '-');

  isAvailable = true;

  profileImageUrl = 'https://via.placeholder.com/150';
  profileImageAlt = 'Logged-in user profile image';

  profileCompletion = 70;

  contactButtonLabel = 'Contact developer';

  changeAvailability() {
    this.isAvailable = !this.isAvailable;
  }

  increaseProfileCompletion() {
    if (this.profileCompletion < 100) {
      this.profileCompletion = this.profileCompletion + 10;
    }
  }

  decreaseProfileCompletion() {
    if (this.profileCompletion > 0) {
      this.profileCompletion = this.profileCompletion - 10;
    }
  }
}
