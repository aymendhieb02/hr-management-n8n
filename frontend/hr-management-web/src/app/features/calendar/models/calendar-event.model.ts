export interface CalendarEvent {
  kind?: 'LEAVE' | 'HOLIDAY';
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
}
