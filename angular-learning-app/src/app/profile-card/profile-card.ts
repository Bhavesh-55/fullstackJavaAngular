import { Component } from '@angular/core';

@Component({
  selector: 'app-profile-card',
  imports: [],
  templateUrl: './profile-card.html',
  styleUrl: './profile-card.css',
})
export class ProfileCard {
  name = 'Bhavesh';
  role = 'Senior Backend Developer';
  experience = '4+ Years';
  currentSkill = 'Java, Spring Boot, Microservices';
  learningSkill = 'Angular';
  location = 'Pune';

  isAvailable = true;

  profileImageUrl = 'https://via.placeholder.com/150';
  profileImageAlt = 'Developer profile image';

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
