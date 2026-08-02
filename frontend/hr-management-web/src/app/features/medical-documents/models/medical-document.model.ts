export interface MedicalDocumentResponse {
  id: number;
  leaveRequestId: number;
  originalFilename: string;
  storedFilename: string;
  mimeType: string;
  fileSize: number;
  uploadedAt: string;
}

export interface MedicalDocumentMetadataResponse {
  id: number;
  leaveRequestId: number;
  originalFilename: string;
  mimeType: string;
  fileSize: number;
  uploadedAt: string;
}
