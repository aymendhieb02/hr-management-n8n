export interface NotificationRecipientSummary {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
}

export interface NotificationResponse {
  id: number;
  recipient: NotificationRecipientSummary;
  title: string;
  message: string;
  read: boolean;
  createdAt: string;
  readAt: string | null;
}
