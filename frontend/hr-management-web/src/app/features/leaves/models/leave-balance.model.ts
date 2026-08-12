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

export interface LeaveBalanceTransaction {
  id: number; balanceId: number; employeeId: number; employeeName: string; requestId: number | null;
  type: string; amount: number; balanceBefore: number; balanceAfter: number; executedAt: string; status: string; error: string | null;
}
