export function overlapsRange(start: string, end: string, rangeStart?: string, rangeEnd?: string): boolean {
  const startTime = new Date(start).getTime();
  const endTime = new Date(end).getTime();
  const rangeStartTime = rangeStart ? new Date(rangeStart).getTime() : Number.NEGATIVE_INFINITY;
  const rangeEndTime = rangeEnd ? new Date(rangeEnd).getTime() : Number.POSITIVE_INFINITY;
  return startTime <= rangeEndTime && endTime >= rangeStartTime;
}

export function isTodayWithin(start: string, end: string, now = new Date()): boolean {
  const today = now.toISOString().slice(0, 10);
  return start <= today && end >= today;
}

export function escapeCsv(value: unknown): string {
  const text = String(value ?? '');
  return /[",\r\n]/.test(text) ? `"${text.replace(/"/g, '""')}"` : text;
}

export function toCsv(rows: Record<string, unknown>[]): string {
  if (!rows.length) return '';
  const headers = Object.keys(rows[0]);
  return [headers.map(escapeCsv).join(','), ...rows.map((row) => headers.map((header) => escapeCsv(row[header])).join(','))].join('\r\n');
}

export function downloadCsv(rows: Record<string, unknown>[], filename: string): void {
  const blob = new Blob([`\uFEFF${toCsv(rows)}`], { type: 'text/csv;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = filename;
  anchor.click();
  URL.revokeObjectURL(url);
}
