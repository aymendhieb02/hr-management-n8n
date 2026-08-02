import { escapeCsv, overlapsRange, toCsv } from './report-utils';

describe('report utilities', () => {
  it('filters inclusive overlapping date ranges', () => {
    expect(overlapsRange('2026-07-10', '2026-07-12', '2026-07-12', '2026-07-14')).toBe(true);
    expect(overlapsRange('2026-07-10', '2026-07-12', '2026-07-13', '2026-07-14')).toBe(false);
  });

  it('escapes CSV commas quotes and line breaks', () => {
    expect(escapeCsv('a,b"c')).toBe('"a,b""c"');
    expect(toCsv([{ Name: 'A,B', Note: 'Line\nTwo' }])).toContain('"A,B","Line\nTwo"');
  });
});
