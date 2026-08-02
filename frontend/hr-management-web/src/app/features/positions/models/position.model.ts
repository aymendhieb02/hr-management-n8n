export interface Position {
  id: number;
  title: string;
  description: string | null;
  createdAt: string;
  updatedAt: string | null;
}

export interface PositionRequest {
  title: string;
  description: string | null;
}
