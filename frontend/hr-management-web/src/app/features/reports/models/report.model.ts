export type ReportType = 'leave-requests' | 'leave-balances' | 'employees' | 'department-summary';

export interface ReportFilters {
  startDate: string;
  endDate: string;
  status: string;
  leaveType: string;
  department: string;
  userId: string;
  year: string;
}
