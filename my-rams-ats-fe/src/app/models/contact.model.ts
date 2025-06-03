export interface ContactMessage {
  id: number;
  name: string;
  email: string;
  phone?: string;
  phoneNumber: string;
  message: string;
  isRead: boolean;
  readAt?: Date;
  readBy?: string;
  createdAt: Date;
  subject?: string;
  priority?: 'low' | 'medium' | 'high';
  repliedAt?: Date;
} 