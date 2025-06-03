import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface ConfirmationDialogData {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  type?: 'danger' | 'warning' | 'info';
  requireDoubleConfirm?: boolean;
  itemName?: string;
}

@Component({
  selector: 'app-confirmation-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './confirmation-dialog.component.html',
  styleUrls: ['./confirmation-dialog.component.css']
})
export class ConfirmationDialogComponent {
  confirmationText = '';
  isDoubleConfirmValid = false;

  constructor(@Inject('dialogData') public data: ConfirmationDialogData) {
    // Set default type if not provided
    if (!this.data.type) {
      this.data.type = 'warning';
    }
  }

  onConfirmationTextChange(): void {
    if (this.data.requireDoubleConfirm && this.data.itemName) {
      this.isDoubleConfirmValid = this.confirmationText.trim() === this.data.itemName.trim();
    }
  }

  onOverlayClick(event: Event): void {
    // Close dialog when clicking overlay
    this.onCancel();
  }

  onConfirm(): void {
    if (this.data.requireDoubleConfirm && !this.isDoubleConfirmValid) {
      return;
    }
    
    // Emit confirmation result
    if (typeof window !== 'undefined') {
      (window as any).confirmDialogResult?.(true);
    }
  }

  onCancel(): void {
    // Emit cancellation result
    if (typeof window !== 'undefined') {
      (window as any).confirmDialogResult?.(false);
    }
  }
} 