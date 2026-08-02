import { LeaveTypeSummary, PersonSummary } from './leave-request.model';

export interface LeaveBalanceRequest {
  userId: number;
  leaveTypeId: number;
  year: number;
  totalDays: number;
}

export interface LeaveBalanceResponse {
  id: number;
  user: PersonSummary;
  leaveType: LeaveTypeSummary;
  year: number;
  totalDays: number;
  usedDays: number;
  remainingDays: number;
  createdAt: string;
  updatedAt: string | null;
}
