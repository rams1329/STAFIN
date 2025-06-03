import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ContactService, ContactMessage } from '../../../services/contact.service';
import { ToastService } from '../../../services/toast.service';
import { DialogService } from '../../../services/dialog.service';

interface FilterOptions {
  search?: string;
  name?: string;
  email?: string;
  message?: string;
  isRead?: boolean;
  readByUser?: string;
  createdDateFrom?: string;
  createdDateTo?: string;
  readDateFrom?: string;
  readDateTo?: string;
}

interface PaginationInfo {
  page: number;
  size: number;
  sort: string;
  direction: 'asc' | 'desc';
  totalElements: number;
  totalPages: number;
}

@Component({
  selector: 'app-contact-messages',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './contact-messages.component.html',
  styleUrls: ['./contact-messages.component.css']
})
export class ContactMessagesComponent implements OnInit {
  messages: ContactMessage[] = [];
  selectedMessage: ContactMessage | null = null;
  loading = false;
  showAdvancedFilters = false;
  unreadCount = 0;
  customPageSize: number = 10;

  filters: FilterOptions = {};

  pagination: PaginationInfo = {
    page: 0,
    size: 10,
    sort: 'createdAt',
    direction: 'desc',
    totalElements: 0,
    totalPages: 0
  };

  constructor(
    private contactService: ContactService,
    private toastService: ToastService,
    private dialogService: DialogService
  ) {}

  ngOnInit(): void {
    this.loadMessages();
    this.loadUnreadCount();
  }

  loadMessages(): void {
    this.loading = true;
    
    console.log('ðŸ”„ Loading messages with filters:', this.filters);
    console.log('ðŸ”„ Pagination:', this.pagination);
    
    // Build query parameters
    const params: any = {
      page: this.pagination.page,
      size: this.pagination.size,
      sort: `${this.pagination.sort},${this.pagination.direction}`
    };

    // Add filter parameters
    Object.keys(this.filters).forEach(key => {
      const value = (this.filters as any)[key];
      if (value !== undefined && value !== null && value !== '') {
        params[key] = value;
      }
    });
    
    this.contactService.getMessagesWithFilters(params).subscribe({
      next: (response: any) => {
        console.log('âœ… Messages API response:', response);
        
        if (response && response.content) {
          this.messages = response.content;
          this.pagination.totalElements = response.totalElements;
          this.pagination.totalPages = response.totalPages;
        } else {
          console.warn('âš ï¸ Unexpected response format:', response);
          this.messages = [];
        }
        
        this.loading = false;
        this.loadUnreadCount();
        
        console.log(`ðŸ“Š Loaded ${this.messages.length} messages successfully`);
      },
      error: (error) => {
        console.error('âŒ Error loading messages:', error);
        this.loading = false;
        this.toastService.error(
          error.message || 'Failed to load contact messages. Please try again.',
          'Load Failed'
        );
      }
    });
  }

  loadUnreadCount(): void {
    this.contactService.getUnreadCount().subscribe({
      next: (response) => {
        this.unreadCount = response.count;
      },
      error: (error) => {
        console.error('Error loading unread count:', error);
      }
    });
  }

  applyFilters(): void {
    this.pagination.page = 0; // Reset to first page when applying filters
    this.loadMessages();
  }

  clearFilters(): void {
    this.filters = {};
    this.pagination.page = 0;
    this.loadMessages();
  }

  sortBy(field: string): void {
    if (this.pagination.sort === field) {
      // Toggle direction if same field
      this.pagination.direction = this.pagination.direction === 'asc' ? 'desc' : 'asc';
    } else {
      // Set new field with default direction
      this.pagination.sort = field;
      this.pagination.direction = 'asc';
    }
    this.pagination.page = 0; // Reset to first page when sorting
    this.loadMessages();
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.pagination.totalPages) {
      this.pagination.page = page;
      this.loadMessages();
    }
  }

  changePageSize(size: number): void {
    if (size && size > 0 && size <= 100) {
      this.pagination.size = size;
      this.pagination.page = 0; // Reset to first page when changing page size
      this.loadMessages();
    }
  }

  viewMessage(message: ContactMessage): void {
    this.selectedMessage = message;
    if (!message.read) {
      this.markAsRead(message);
    }
  }

  closeMessageDetails(): void {
    this.selectedMessage = null;
  }

  markAsRead(message: ContactMessage): void {
    if (!message.read) {
      this.contactService.markAsRead(message.id!).subscribe({
        next: (updatedMessage) => {
          message.read = true;
          message.readAt = updatedMessage.readAt;
          message.readBy = updatedMessage.readBy;
          this.unreadCount = Math.max(0, this.unreadCount - 1);
          this.toastService.success('Message marked as read', 'Success');
        },
        error: (error) => {
          this.toastService.error(
            error.message || 'Failed to mark message as read',
            'Error'
          );
        }
      });
    }
  }

  markAllAsRead(): void {
    if (this.unreadCount === 0) return;
    
    this.dialogService.confirm({
      title: 'Mark All Messages as Read',
      message: 'Are you sure you want to mark all messages as read? This action will update all unread messages.',
      type: 'info',
      confirmText: 'Mark All as Read',
      cancelText: 'Cancel'
    }).then((confirmed) => {
      if (confirmed) {
        this.contactService.markAllAsRead().subscribe({
          next: () => {
            this.messages.forEach(message => {
              if (!message.read) {
                message.read = true;
              }
            });
            this.unreadCount = 0;
            this.toastService.success('All messages marked as read', 'Success');
          },
          error: (error) => {
            this.toastService.error(
              error.message || 'Failed to mark all messages as read',
              'Error'
            );
          }
        });
      }
    });
  }

  deleteMessage(message: ContactMessage): void {
    this.dialogService.confirm({
      title: 'Delete Contact Message',
      message: `Are you sure you want to delete the message from "${message.name}"? This action cannot be undone.`,
      type: 'danger',
      confirmText: 'Delete Message',
      cancelText: 'Cancel'
    }).then((confirmed) => {
      if (confirmed) {
        this.contactService.deleteMessage(message.id!).subscribe({
          next: () => {
            this.toastService.success('Message deleted successfully', 'Success');
            this.loadMessages(); // Reload the messages list
          },
          error: (error) => {
            this.toastService.error(
              error.message || 'Failed to delete message',
              'Error'
            );
          }
        });
      }
    });
  }
}

