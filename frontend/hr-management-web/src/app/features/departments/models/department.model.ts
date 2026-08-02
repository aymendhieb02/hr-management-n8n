export interface Department {
  id: number;
  name: string;
  description: string | null;
  createdAt: string;
  updatedAt: string | null;
}

export interface DepartmentRequest {
  name: string;
  description: string | null;
}
