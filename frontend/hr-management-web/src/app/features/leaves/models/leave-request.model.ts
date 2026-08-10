export type LeaveRequestStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';
export type LeaveRequestNature = 'CONGE' | 'AUTORISATION_ABSENCE';

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
  nature: LeaveRequestNature;
  reasonId: number | null;
  otherReason: string | null;
  startDate: string;
  endDate: string;
  startTime: string | null;
  endTime: string | null;
  numberOfDays: number;
  reason: string | null;
}

export interface LeaveRequestUpdateRequest {
  leaveTypeId: number;
  nature: LeaveRequestNature;
  reasonId: number | null;
  otherReason: string | null;
  startDate: string;
  endDate: string;
  startTime: string | null;
  endTime: string | null;
  numberOfDays: number;
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
  nature: LeaveRequestNature;
  startDate: string;
  endDate: string;
  startTime: string | null;
  endTime: string | null;
  requestedDays: number;
  reason: string | null;
  status: LeaveRequestStatus;
  submittedAt: string;
  decisionAt: string | null;
  decisionComment: string | null;
}
