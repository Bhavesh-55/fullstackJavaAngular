import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export function noOnlySpacesValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value || '';

    if (value.length > 0 && value.trim().length === 0) {
      return {
        onlySpaces: true
      };
    }

    return null;
  };
}

export function noNumbersValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value || '';

    const hasNumber = /\d/.test(value);

    if (hasNumber) {
      return {
        numbersNotAllowed: true
      };
    }

    return null;
  };
}

export function skillCategoryMatchValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const skillName = control.get('name')?.value?.trim().toLowerCase();
    const category = control.get('category')?.value;

    if (!skillName || !category) {
      return null;
    }

    const frontendSkills = ['angular', 'react', 'html', 'css', 'javascript', 'typescript'];

    const backendSkills = ['java', 'spring', 'spring boot', 'microservices', 'hibernate', 'jpa'];

    const cloudSkills = ['aws', 'azure', 'gcp'];

    const databaseSkills = ['mysql', 'postgresql', 'mongodb', 'oracle'];

    const devOpsSkills = ['docker', 'kubernetes', 'jenkins', 'gitlab', 'terraform'];

    if (frontendSkills.includes(skillName) && category !== 'Frontend') {
      return {
        categoryMismatch: {
          expected: 'Frontend'
        }
      };
    }

    if (backendSkills.includes(skillName) && category !== 'Backend') {
      return {
        categoryMismatch: {
          expected: 'Backend'
        }
      };
    }

    if (cloudSkills.includes(skillName) && category !== 'Cloud') {
      return {
        categoryMismatch: {
          expected: 'Cloud'
        }
      };
    }

    if (databaseSkills.includes(skillName) && category !== 'Database') {
      return {
        categoryMismatch: {
          expected: 'Database'
        }
      };
    }

    if (devOpsSkills.includes(skillName) && category !== 'DevOps') {
      return {
        categoryMismatch: {
          expected: 'DevOps'
        }
      };
    }

    return null;
  };
}