import { Component } from '@angular/core';
import { ProfileCard } from '../../profile-card/profile-card';

@Component({
  selector: 'app-profile-page',
  imports: [ProfileCard],
  templateUrl: './profile-page.html',
  styleUrl: './profile-page.css',
})
export class ProfilePage {}
