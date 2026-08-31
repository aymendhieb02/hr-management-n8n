export type LeaveRequestStatus = 'DRAFT' | 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';
export type LeaveRequestNature = 'CONGE' | 'AUTORISATION_ABSENCE';

export interface PersonSummary {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  photoUrl?: string | null;
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
  medicalCertificateRequired?: boolean;
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
  saturdayCounts?: boolean;
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
  reasonId?: number|null;
  medicalCertificateRequired?: boolean;
  status: LeaveRequestStatus;
  submittedAt: string;
  dateCreation?: string | null;
  decisionAt: string | null;
  decisionComment: string | null;
  saturdayCounts?: boolean;
  consumedDays?: number | null;
  actualEndDate?: string | null;
  regularizedAt?: string | null;
  regularizedBy?: PersonSummary | null;
  regularizationComment?: string | null;
  workflow?: { status:string; currentStep:number|null; steps:{id:number;priority:number;status:string;approver:PersonSummary;comment:string|null;actedAt:string|null}[] } | null;
}
