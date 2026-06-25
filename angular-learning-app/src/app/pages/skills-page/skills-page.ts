import { Component } from '@angular/core';
import { SkillList } from '../../skill-list/skill-list';

@Component({
  selector: 'app-skills-page',
  imports: [SkillList],
  templateUrl: './skills-page.html',
  styleUrl: './skills-page.css',
})
export class SkillsPage {}
