import { Component, OnInit, inject ,signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { Skill, ApiErrorResponse ,SkillRequest} from '../models/skill.model';
import { SkillService } from '../services/skill';
import { SkillCard } from '../skill-card/skill-card';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../services/auth';

@Component({
  selector: 'app-skill-list',
  imports: [ReactiveFormsModule,FormsModule, SkillCard],
  templateUrl: './skill-list.html',
  styleUrl: './skill-list.css'
})
export class SkillList implements OnInit {
  private skillService = inject(SkillService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);

  
  skills = signal<Skill[]>([]);
  isLoading = signal(false);
  isSaving = signal(false);
  deletingSkillId = signal<number | null>(null);

  isEditMode = signal(false);
  editingSkillId = signal<number | null>(null);

  errorMessage = signal('');
  successMessage = signal('');
  backendValidationErrors = signal<Record<string, string>>({});

  //pagination and sorting
  pageNumber = signal(0);
  pageSize = signal(5);
  totalElements = signal(0);
  totalPages = signal(0);
  last = signal(false);
  sortBy = 'id';
  sortDir = 'asc';

  selectedCategory = '';
  selectedLevel = '';
  searchText = '';

  skillForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    category: ['Frontend', Validators.required],
    level: ['Beginner', Validators.required],
    description: ['', Validators.maxLength(500)]
  });

  ngOnInit() {
    this.route.queryParamMap.subscribe(params => {
      this.selectedCategory = params.get('category') || '';
      this.selectedLevel = params.get('level') || '';
      this.searchText = params.get('search') || '';

      this.loadSkillsFromBackend();
    });
  }

  get nameControl() {
    return this.skillForm.controls.name;
  }

  get categoryControl() {
    return this.skillForm.controls.category;
  }

  get levelControl() {
    return this.skillForm.controls.level;
  }

  get descriptionControl() {
    return this.skillForm.controls.description;
  }

  loadSkillsFromBackend() {
  this.isLoading.set(true);
  this.errorMessage.set('');

  this.skillService.getSkills({
    category: this.selectedCategory,
    level: this.selectedLevel,
    search: this.searchText,
    page: this.pageNumber(),
    size: this.pageSize(),
    sortBy: this.sortBy,
    sortDir: this.sortDir
  }).subscribe({
    next: pageResponse => {
      this.skills.set(pageResponse.content);
      this.pageNumber.set(pageResponse.pageNumber);
      this.pageSize.set(pageResponse.pageSize);
      this.totalElements.set(pageResponse.totalElements);
      this.totalPages.set(pageResponse.totalPages);
      this.last.set(pageResponse.last);

      this.isLoading.set(false);
    },
    error: error => {
      this.handleLoadError(error);
      this.isLoading.set(false);
    }
  });
}

  //pagination and sorting methods
  goToFirstPage() {
  this.pageNumber.set(0);
  this.loadSkillsFromBackend();
}

goToPreviousPage() {
  if (this.pageNumber() > 0) {
    this.pageNumber.set(this.pageNumber() - 1);
    this.loadSkillsFromBackend();
  }
}

goToNextPage() {
  if (!this.last()) {
    this.pageNumber.set(this.pageNumber() + 1);
    this.loadSkillsFromBackend();
  }
}

goToLastPage() {
  if (this.totalPages() > 0) {
    this.pageNumber.set(this.totalPages() - 1);
    this.loadSkillsFromBackend();
  }
}

onPageSizeChange() {
  this.pageNumber.set(0);
  this.loadSkillsFromBackend();
}

onSortChange() {
  this.pageNumber.set(0);
  this.loadSkillsFromBackend();
}

  saveSkill() {
    this.successMessage.set('');
    this.errorMessage.set('');
    this.backendValidationErrors.set({});

    if (this.skillForm.invalid) {
      this.skillForm.markAllAsTouched();
      return;
    }

    const formValue = this.skillForm.getRawValue();

    const request: SkillRequest = {
      name: formValue.name.trim(),
      category: formValue.category,
      level: formValue.level,
      description: formValue.description.trim()
    };

    if (this.isEditMode()) {
      this.updateSkill(request);
    } else {
      this.createSkill(request);
    }
  }

  createSkill(request: SkillRequest) {
    this.isSaving.set(true);

    this.skillService.createSkill(request).subscribe({
      next: savedSkill => {
        this.successMessage.set(`Skill "${savedSkill.name}" added successfully.`);

        this.resetForm();
        this.isSaving.set(false);

        this.loadSkillsFromBackend();
      },
      error: error => {
        this.handleSaveError(error);
        this.isSaving.set(false);
      }
    });
  }

  updateSkill(request: SkillRequest) {
    const skillId = this.editingSkillId();

    if (skillId === null) {
      this.errorMessage.set('No skill selected for update.');
      return;
    }

    this.isSaving.set(true);

    this.skillService.updateSkill(skillId, request).subscribe({
      next: updatedSkill => {
        this.successMessage.set(`Skill "${updatedSkill.name}" updated successfully.`);

        this.resetForm();
        this.isSaving.set(false);

        this.loadSkillsFromBackend();
      },
      error: error => {
        this.handleSaveError(error);
        this.isSaving.set(false);
      }
    });
  }

  startEdit(skill: Skill) {
    this.isEditMode.set(true);
    this.editingSkillId.set(skill.id);

    this.successMessage.set('');
    this.errorMessage.set('');
    this.backendValidationErrors.set({});

    this.skillForm.patchValue({
      name: skill.name,
      category: skill.category,
      level: skill.level,
      description: skill.description || ''
    });

    window.scrollTo({
      top: 0,
      behavior: 'smooth'
    });
  }

  cancelEdit() {
    this.resetForm();
  }

  deleteSkill(skillId: number) {
    const confirmed = confirm('Are you sure you want to delete this skill?');

    if (!confirmed) {
      return;
    }

    this.successMessage.set('');
    this.errorMessage.set('');

    this.deletingSkillId.set(skillId);

    this.skillService.deleteSkill(skillId).subscribe({
      next: () => {
        this.successMessage.set('Skill deleted successfully.');

        this.deletingSkillId.set(null);

        if (this.editingSkillId() === skillId) {
          this.resetForm();
        }

        this.loadSkillsFromBackend();
      },
      error: error => {
        this.handleDeleteError(error);
        this.deletingSkillId.set(null);
      }
    });
  }

  applyFilter() {
  this.pageNumber.set(0);

  this.router.navigate(['/skills'], {
    queryParams: {
      category: this.selectedCategory || null,
      level: this.selectedLevel || null,
      search: this.searchText.trim() || null
    }
  });
}

  clearFilters() {
    this.selectedCategory = '';
    this.selectedLevel = '';
    this.searchText = '';

    this.router.navigate(['/skills']);
  }

  refreshSkills() {
    this.loadSkillsFromBackend();
  }

  private resetForm() {
    this.skillForm.reset({
      name: '',
      category: 'Frontend',
      level: 'Beginner',
      description: ''
    });

    this.isEditMode.set(false);
    this.editingSkillId.set(null);
    this.backendValidationErrors.set({});
  }

  private handleLoadError(error: HttpErrorResponse) {
    console.error('Load skills API failed', error);

    if (error.status === 0) {
      this.errorMessage.set('Backend is not reachable. Please check if Spring Boot is running.');
    } else if (error.status === 500) {
      this.errorMessage.set('Backend server error. Please check Spring Boot logs.');
    } else {
      const apiError = error.error as ApiErrorResponse;
      this.errorMessage.set(apiError?.message || 'Unable to load skills from backend.');
    }

    this.skills.set([]);
  }

  private handleSaveError(error: HttpErrorResponse) {
    console.error('Save skill API failed', error);

    const apiError = error.error as ApiErrorResponse;

    if (error.status === 0) {
      this.errorMessage.set('Backend is not reachable. Please check if Spring Boot is running.');
      return;
    }

    if (error.status === 400) {
      this.errorMessage.set(apiError?.message || 'Validation failed.');
      this.backendValidationErrors.set(apiError?.validationErrors || {});
      return;
    }

    if (error.status === 409) {
      this.errorMessage.set(apiError?.message || 'Skill already exists.');
      return;
    }

    if (error.status === 404) {
      this.errorMessage.set(apiError?.message || 'Skill not found.');
      return;
    }

    if (error.status === 500) {
      this.errorMessage.set('Backend server error. Please check Spring Boot logs.');
      return;
    }

    this.errorMessage.set(apiError?.message || 'Unable to save skill.');
  }

  private handleDeleteError(error: HttpErrorResponse) {
    console.error('Delete skill API failed', error);

    const apiError = error.error as ApiErrorResponse;

    if (error.status === 0) {
      this.errorMessage.set('Backend is not reachable. Please check if Spring Boot is running.');
      return;
    }

    if (error.status === 404) {
      this.errorMessage.set(apiError?.message || 'Skill not found.');
      return;
    }

    if (error.status === 500) {
      this.errorMessage.set('Backend server error. Please check Spring Boot logs.');
      return;
    }

    this.errorMessage.set(apiError?.message || 'Unable to delete skill.');
  }


  isAdmin() {
  return this.authService.isAdmin();
  }
}