export interface CalendarEvent {
  kind?: 'LEAVE' | 'HOLIDAY' | 'RESTORED' | 'HALF_DAY';
  eventId?: string;
  leaveRequestId: number;
  userId: number;
  employeeName: string;
  employeePhotoUrl?: string | null;
  leaveTypeName: string;
  departmentName: string | null;
  startDate: string;
  endDate: string;
  status: string;
  nature: 'CONGE' | 'AUTORISATION_ABSENCE';
  startTime: string | null;
  endTime: string | null;
  reason: string | null;
  description?: string | null;
  consumedDays?: number|null;
  actualEndDate?: string|null;
  regularizedAt?: string|null;
  regularizedBy?: string|null;
  regularizationComment?: string|null;
}
