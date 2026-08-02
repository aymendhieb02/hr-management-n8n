import { HttpErrorResponse } from '@angular/common/http';

export function safeApiMessage(error: unknown, fallback: string): string {
  if (!(error instanceof HttpErrorResponse)) {
    return fallback;
  }

  if (error.status === 409) {
    return 'This item cannot be deleted or saved because it is referenced elsewhere.';
  }

  if (error.status === 400 && error.error?.validationErrors) {
    return 'Please fix the highlighted fields and try again.';
  }

  return fallback;
}

export function validationErrors(error: unknown): Record<string, string> {
  if (error instanceof HttpErrorResponse && error.error?.validationErrors) {
    return error.error.validationErrors as Record<string, string>;
  }

  return {};
}
