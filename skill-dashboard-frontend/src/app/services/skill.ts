import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Skill,SkillFilter, SkillRequest ,PageResponse } from '../models/skill.model';
import { environment } from '../../environments/environment';
import { SkillDocument } from '../models/skill-document.model';

@Injectable({
  providedIn: 'root'
})
export class SkillService {
  private http = inject(HttpClient);

  private readonly apiUrl = `${environment.apiBaseUrl}/skills`;

getSkills(filter?: SkillFilter): Observable<PageResponse<Skill>> {
  let params = new HttpParams();

  if (filter?.category) {
    params = params.set('category', filter.category);
  }

  if (filter?.level) {
    params = params.set('level', filter.level);
  }

  if (filter?.search?.trim()) {
    params = params.set('search', filter.search.trim());
  }

  params = params.set('page', filter?.page ?? 0);
  params = params.set('size', filter?.size ?? 5);
  params = params.set('sortBy', filter?.sortBy || 'id');
  params = params.set('sortDir', filter?.sortDir || 'asc');

  return this.http.get<PageResponse<Skill>>(this.apiUrl, { params });
}






    getSkillById(skillId: number): Observable<Skill> {
    return this.http.get<Skill>(`${this.apiUrl}/${skillId}`);
  }

    createSkill(request: SkillRequest): Observable<Skill> {
    return this.http.post<Skill>(this.apiUrl, request);
  }

    updateSkill(skillId: number, request: SkillRequest): Observable<Skill> {
    return this.http.put<Skill>(`${this.apiUrl}/${skillId}`, request);
  }

  deleteSkill(skillId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${skillId}`);
  }


  //documents upload, download, delete
  uploadDocument(skillId: number, file: File) {
  const formData = new FormData();

  formData.append('file', file);

  return this.http.post<SkillDocument>(
    `${this.apiUrl}/${skillId}/documents`,
    formData
  );
}

getDocuments(skillId: number) {
  return this.http.get<SkillDocument[]>(
    `${this.apiUrl}/${skillId}/documents`
  );
}

downloadDocument(documentId: number) {
  return this.http.get(
    `${this.apiUrl}/documents/${documentId}/download`,
    {
      responseType: 'blob'
    }
  );
}

deleteDocument(documentId: number) {
  return this.http.delete<void>(
    `${this.apiUrl}/documents/${documentId}`
  );
}

}