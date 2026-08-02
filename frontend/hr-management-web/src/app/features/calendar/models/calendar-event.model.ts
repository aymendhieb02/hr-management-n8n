export interface CalendarEvent {
  leaveRequestId: number;
  userId: number;
  employeeName: string;
  leaveTypeName: string;
  departmentName: string | null;
  startDate: string;
  endDate: string;
  status: string;
}
