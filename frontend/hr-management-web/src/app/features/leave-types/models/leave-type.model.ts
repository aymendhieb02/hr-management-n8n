export interface LeaveType {
  id: number;
  name: string;
  description: string | null;
  maxDays: number | null;
  requiresMedicalCertificate: boolean;
  active: boolean;
  createdAt: string;
  updatedAt: string | null;
}

export interface LeaveTypeRequest {
  name: string;
  description: string | null;
  maxDays: number | null;
  requiresMedicalCertificate: boolean;
  active: boolean;
}
