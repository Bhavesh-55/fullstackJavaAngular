import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SkillCard } from './skill-card';

describe('SkillCard', () => {
  let component: SkillCard;
  let fixture: ComponentFixture<SkillCard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SkillCard],
    }).compileComponents();

    fixture = TestBed.createComponent(SkillCard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
