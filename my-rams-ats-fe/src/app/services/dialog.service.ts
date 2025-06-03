import { Injectable, ComponentRef, ViewContainerRef, ApplicationRef, ComponentFactoryResolver, Injector } from '@angular/core';
import { ConfirmationDialogComponent, ConfirmationDialogData } from '../components/shared/confirmation-dialog/confirmation-dialog.component';

@Injectable({
  providedIn: 'root'
})
export class DialogService {
  private dialogRef: ComponentRef<ConfirmationDialogComponent> | null = null;

  constructor(
    private appRef: ApplicationRef,
    private componentFactoryResolver: ComponentFactoryResolver,
    private injector: Injector
  ) {}

  /**
   * Show a confirmation dialog
   */
  confirm(data: ConfirmationDialogData): Promise<boolean> {
    return new Promise((resolve) => {
      // Remove any existing dialog
      this.closeDialog();

      // Create component factory
      const componentFactory = this.componentFactoryResolver.resolveComponentFactory(ConfirmationDialogComponent);
      
      // Create injector with data
      const injector = Injector.create({
        providers: [
          { provide: 'dialogData', useValue: data }
        ],
        parent: this.injector
      });

      // Create component
      this.dialogRef = componentFactory.create(injector);

      // Set up result callback
      (window as any).confirmDialogResult = (result: boolean) => {
        resolve(result);
        this.closeDialog();
        delete (window as any).confirmDialogResult;
      };

      // Attach to application
      this.appRef.attachView(this.dialogRef.hostView);

      // Append to body
      const domElem = (this.dialogRef.hostView as any).rootNodes[0] as HTMLElement;
      document.body.appendChild(domElem);
    });
  }

  /**
   * Simple confirmation dialog
   */
  simpleConfirm(message: string, title: string = 'Confirm Action'): Promise<boolean> {
    return this.confirm({
      title,
      message,
      type: 'warning',
      confirmText: 'Yes',
      cancelText: 'Cancel'
    });
  }

  /**
   * Danger confirmation dialog (for destructive actions)
   */
  dangerConfirm(message: string, title: string = 'Confirm Delete', confirmText: string = 'Delete'): Promise<boolean> {
    return this.confirm({
      title,
      message,
      type: 'danger',
      confirmText,
      cancelText: 'Cancel'
    });
  }

  /**
   * Double confirmation dialog (requires typing item name)
   */
  doubleConfirm(message: string, itemName: string, title: string = 'Confirm Delete'): Promise<boolean> {
    return this.confirm({
      title,
      message,
      type: 'danger',
      confirmText: 'Delete',
      cancelText: 'Cancel',
      requireDoubleConfirm: true,
      itemName
    });
  }

  /**
   * Delete user confirmation with double confirmation
   */
  confirmDeleteUser(userName: string): Promise<boolean> {
    return this.doubleConfirm(
      `Are you sure you want to delete user "${userName}"? This action cannot be undone and will permanently remove all user data.`,
      userName,
      'Delete User'
    );
  }

  /**
   * Delete message confirmation
   */
  confirmDeleteMessage(senderName: string): Promise<boolean> {
    return this.dangerConfirm(
      `Are you sure you want to delete the message from ${senderName}? This action cannot be undone.`,
      'Delete Message'
    );
  }

  /**
   * Delete education record confirmation
   */
  confirmDeleteEducation(): Promise<boolean> {
    return this.dangerConfirm(
      'Are you sure you want to delete this education record? This action cannot be undone.',
      'Delete Education'
    );
  }

  /**
   * Delete employment record confirmation
   */
  confirmDeleteEmployment(): Promise<boolean> {
    return this.dangerConfirm(
      'Are you sure you want to delete this employment record? This action cannot be undone.',
      'Delete Employment'
    );
  }

  /**
   * Reset password confirmation
   */
  confirmResetPassword(userName: string): Promise<boolean> {
    return this.confirm({
      title: 'Reset Password',
      message: `Are you sure you want to reset the password for ${userName}? A new password will be sent to their email.`,
      type: 'warning',
      confirmText: 'Reset Password',
      cancelText: 'Cancel'
    });
  }

  /**
   * Close the current dialog
   */
  private closeDialog(): void {
    if (this.dialogRef) {
      this.appRef.detachView(this.dialogRef.hostView);
      this.dialogRef.destroy();
      this.dialogRef = null;
    }
  }
} 