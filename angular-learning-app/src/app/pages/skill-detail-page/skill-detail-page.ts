
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Skill } from '../../models/skill.model';

import { Component, OnInit, inject ,signal} from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

import { SkillDocument } from '../../models/skill-document.model';
import { SkillService } from '../../services/skill';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-skill-detail-page',
  imports: [RouterLink],
  templateUrl: './skill-detail-page.html',
  styleUrl: './skill-detail-page.css',
})
export class SkillDetailPage  implements OnInit  {

  private route = inject(ActivatedRoute);
  private skillService = inject(SkillService);
  private authService = inject(AuthService);

  skill = signal<Skill | null>(null);
  documents = signal<SkillDocument[]>([]);

  isLoading = signal(false);
  isUploading = signal(false);
  errorMessage = signal('');
  successMessage = signal('');

  selectedFile = signal<File | null>(null);

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const skillId = Number(params.get('id'));

      if (!skillId) {
        this.errorMessage.set('Invalid skill id.');
        return;
      }

      this.loadSkillFromBackend(skillId);
       this.loadDocuments(skillId);
    });
  }

  isAdmin() {
    return this.authService.isAdmin();
  }

  loadSkillFromBackend(skillId: number) {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.skillService.getSkillById(skillId).subscribe({
      next: skill => {
        this.skill.set(skill);
        this.isLoading.set(false);
      },
      error: error => {
        this.handleError(error);
      }
    });
  }

  private handleError(error: HttpErrorResponse) {
    console.error('Skill detail API failed', error);

    if (error.status === 0) {
      this.errorMessage.set('Backend is not reachable. Please check if Spring Boot is running.');
    } else if (error.status === 404) {
      this.errorMessage.set('Skill not found in database.');
    } else {
      this.errorMessage.set(error.error?.message || 'Unable to load skill details.');
    }

    this.skill.set(null);
    this.isLoading.set(false);
  }

    loadDocuments(skillId: number) {
    this.skillService.getDocuments(skillId).subscribe({
      next: documents => {
        this.documents.set(documents);
      },
      error: error => {
        console.error(error);
      }
    });
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;

    if (!input.files || input.files.length === 0) {
      this.selectedFile.set(null);
      return;
    }

    this.selectedFile.set(input.files[0]);
  }

  uploadDocument() {
    const currentSkill = this.skill();
    const file = this.selectedFile();

    if (!currentSkill) {
      this.errorMessage.set('Skill not loaded.');
      return;
    }

    if (!file) {
      this.errorMessage.set('Please select a file.');
      return;
    }

    this.errorMessage.set('');
    this.successMessage.set('');
    this.isUploading.set(true);

    this.skillService.uploadDocument(currentSkill.id, file).subscribe({
      next: uploadedDocument => {
        this.successMessage.set(`File "${uploadedDocument.originalFileName}" uploaded successfully.`);
        this.selectedFile.set(null);
        this.isUploading.set(false);

        this.loadDocuments(currentSkill.id);
      },
      error: error => {
        console.error(error);
        this.errorMessage.set(error.error?.message || 'Unable to upload file.');
        this.isUploading.set(false);
      }
    });
  }

downloadDocument(file: SkillDocument) {
  this.skillService.downloadDocument(file.id).subscribe({
    next: blob => {
      const fileUrl = window.URL.createObjectURL(blob);

      const anchor = window.document.createElement('a');
      anchor.href = fileUrl;
      anchor.download = file.originalFileName;
      anchor.click();

      window.URL.revokeObjectURL(fileUrl);
    },
    error: error => {
      console.error(error);
      this.errorMessage.set('Unable to download file.');
    }
  });
}

  deleteDocument(documentId: number) {
    const currentSkill = this.skill();

    if (!currentSkill) {
      return;
    }

    const confirmed = confirm('Are you sure you want to delete this document?');

    if (!confirmed) {
      return;
    }

    this.skillService.deleteDocument(documentId).subscribe({
      next: () => {
        this.successMessage.set('Document deleted successfully.');
        this.loadDocuments(currentSkill.id);
      },
      error: error => {
        console.error(error);
        this.errorMessage.set('Unable to delete document.');
      }
    });
  }

  formatFileSize(size: number) {
    if (size < 1024) {
      return `${size} B`;
    }

    if (size < 1024 * 1024) {
      return `${(size / 1024).toFixed(1)} KB`;
    }

    return `${(size / (1024 * 1024)).toFixed(1)} MB`;
  }

}