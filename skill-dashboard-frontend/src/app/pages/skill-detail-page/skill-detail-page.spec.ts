import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SkillDetailPage } from './skill-detail-page';

describe('SkillDetailPage', () => {
  let component: SkillDetailPage;
  let fixture: ComponentFixture<SkillDetailPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SkillDetailPage],
    }).compileComponents();

    fixture = TestBed.createComponent(SkillDetailPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
