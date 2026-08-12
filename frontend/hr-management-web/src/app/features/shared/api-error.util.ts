import { HttpErrorResponse } from '@angular/common/http';

export function safeApiMessage(error: unknown, fallback: string): string {
  if (!(error instanceof HttpErrorResponse)) {
    return fallback;
  }

  if (error.status === 400 && error.error?.validationErrors) {
    const messages = Object.values(error.error.validationErrors).filter((value): value is string => typeof value === 'string');
    return messages.length ? messages.join(' ') : 'Veuillez corriger les champs indiqués.';
  }

  const apiMessage = typeof error.error?.message === 'string' ? error.error.message.trim() : '';
  if (apiMessage && apiMessage !== 'Validation failed') return apiMessage;
  if (error.status === 409) return 'Cette opération est impossible car cet élément est déjà utilisé.';

  return fallback;
}

export function validationErrors(error: unknown): Record<string, string> {
  if (error instanceof HttpErrorResponse && error.error?.validationErrors) {
    return error.error.validationErrors as Record<string, string>;
  }

  return {};
}
