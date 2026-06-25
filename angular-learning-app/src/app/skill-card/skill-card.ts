import { Component, input ,output } from '@angular/core';
import { Skill } from '../models/skill.model';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-skill-card',
  imports: [RouterLink],
  templateUrl: './skill-card.html',
  styleUrl: './skill-card.css',
})
export class SkillCard {

  skill = input.required<Skill>();
  index = input.required<number>();
  isDeleting = input(false);
  canManageSkill = input(false);

  editSkill = output<Skill>();
  deleteSkill = output<number>();

  onEditClick() {
    this.editSkill.emit(this.skill());
  }

  onDeleteClick() {
    this.deleteSkill.emit(this.skill().id);
  }

}
