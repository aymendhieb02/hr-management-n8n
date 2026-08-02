export type LeaveRequestStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';

export interface PersonSummary {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
}

export interface LeaveTypeSummary {
  id: number;
  name: string;
}

export interface LeaveRequestCreateRequest {
  requesterId: number;
  leaveTypeId: number;
  startDate: string;
  endDate: string;
  reason: string | null;
}

export interface LeaveRequestUpdateRequest {
  leaveTypeId: number;
  startDate: string;
  endDate: string;
  reason: string | null;
}

export interface LeaveDecisionRequest {
  approverId: number;
  comment: string | null;
}

export interface LeaveRequestResponse {
  id: number;
  requester: PersonSummary;
  approver: PersonSummary | null;
  leaveType: LeaveTypeSummary;
  startDate: string;
  endDate: string;
  requestedDays: number;
  reason: string | null;
  status: LeaveRequestStatus;
  submittedAt: string;
  decisionAt: string | null;
  decisionComment: string | null;
}
