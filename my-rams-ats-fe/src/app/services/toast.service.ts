import { Injectable } from '@angular/core';
import { ToastrService } from 'ngx-toastr';

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  constructor(private toastr: ToastrService) {}
  
  success(message: string, title: string = 'Success') {
    this.toastr.success(message, title, {
      timeOut: 3000,
      closeButton: true,
      progressBar: true,
      positionClass: 'toast-top-right'
    });
  }
  
  error(message: string, title: string = 'Error') {
    this.toastr.error(message, title, {
      timeOut: 5000,
      closeButton: true,
      progressBar: true,
      positionClass: 'toast-top-right'
    });
  }
  
  info(message: string, title: string = 'Info') {
    this.toastr.info(message, title, {
      timeOut: 3000,
      closeButton: true,
      progressBar: true,
      positionClass: 'toast-top-right'
    });
  }
  
  warning(message: string, title: string = 'Warning') {
    this.toastr.warning(message, title, {
      timeOut: 4000,
      closeButton: true,
      progressBar: true,
      positionClass: 'toast-top-right'
    });
  }

  // Quick success methods for common scenarios
  loginSuccess(userName?: string) {
    this.success(
      userName ? `Welcome back, ${userName}!` : 'Welcome back to the admin panel!',
      'Login Successful'
    );
  }

  jobCreated(jobTitle?: string) {
    this.success(
      jobTitle ? `Job "${jobTitle}" has been successfully created.` : 'The job posting has been successfully created.',
      'Job Created'
    );
  }

  jobUpdated(jobTitle?: string) {
    this.success(
      jobTitle ? `Job "${jobTitle}" has been successfully updated.` : 'The job posting has been successfully updated.',
      'Job Updated'
    );
  }

  profileUpdated() {
    this.success('Your profile has been updated successfully.', 'Profile Updated');
  }

  passwordChanged() {
    this.success('Your password has been changed successfully.', 'Password Changed');
  }

  contactMessageSent() {
    this.success('Thank you for your message! We will get back to you soon.', 'Message Sent');
  }

  messageMarkedAsRead() {
    this.success('Message marked as read', 'Updated');
  }

  allMessagesMarkedAsRead(count: number) {
    this.success(`All ${count} messages marked as read`, 'Bulk Update Complete');
  }

  messageDeleted(senderName?: string) {
    this.success(
      senderName ? `Message from ${senderName} deleted successfully` : 'Message deleted successfully',
      'Message Deleted'
    );
  }

  bulkActionComplete(action: string, count: number) {
    this.success(`${action} completed for ${count} items`, 'Bulk Action Complete');
  }

  // Job management methods
  jobStatusChanged(jobTitle: string, isActive: boolean) {
    this.success(
      `Job "${jobTitle}" has been ${isActive ? 'activated' : 'deactivated'} successfully.`,
      'Status Updated'
    );
  }

  jobDeleted(jobTitle?: string) {
    this.success(
      jobTitle ? `Job "${jobTitle}" has been deleted successfully.` : 'Job has been deleted successfully.',
      'Job Deleted'
    );
  }

  noJobsFound() {
    this.info('No job postings found. Create your first job to get started.', 'No Jobs');
  }

  jobsLoaded(count: number) {
    if (count > 0) {
      this.info(`Successfully loaded ${count} job${count === 1 ? '' : 's'}.`, 'Jobs Loaded');
    }
  }

  // Quick error methods
  loginFailed(message?: string) {
    this.error(message || 'Invalid credentials. Please try again.', 'Login Failed');
  }

  apiError(message?: string) {
    this.error(message || 'Something went wrong. Please try again.', 'Action Failed');
  }

  validationError(message?: string) {
    this.error(message || 'Please fill all required fields correctly.', 'Validation Error');
  }

  // Quick warning methods
  sessionExpiring() {
    this.warning('Your session will expire in 2 minutes. Please save your work.', 'Session Expiring');
  }

  unsavedChanges() {
    this.warning('You have unsaved changes. Make sure to save before leaving.', 'Unsaved Changes');
  }

  // ========================================
  // USER MANAGEMENT METHODS
  // ========================================

  userCreated(userName?: string, hasOtp?: boolean) {
    const message = hasOtp 
      ? `User "${userName}" created successfully. OTP has been sent to their email for first login.`
      : `User "${userName}" has been created successfully.`;
    this.success(message, 'User Created');
  }

  userUpdated(userName?: string) {
    this.success(
      userName ? `User "${userName}" has been updated successfully.` : 'User has been updated successfully.',
      'User Updated'
    );
  }

  userDeleted(userName?: string) {
    this.success(
      userName ? `User "${userName}" has been deleted successfully.` : 'User has been deleted successfully.',
      'User Deleted'
    );
  }

  userStatusChanged(userName: string, isActive: boolean) {
    this.success(
      `User "${userName}" has been ${isActive ? 'activated' : 'deactivated'} successfully.`,
      'Status Updated'
    );
  }

  passwordReset(userName?: string) {
    this.success(
      userName ? `Password reset email sent to ${userName}` : 'Password reset email has been sent.',
      'Password Reset'
    );
  }

  roleAssignmentFailed() {
    this.error('Failed to assign roles to user. Please try again.', 'Role Assignment Failed');
  }

  emailAlreadyExists() {
    this.error('This email address is already in use. Please choose a different email.', 'Email Already Exists');
  }

  rolesLoaded(count: number) {
    if (count > 0) {
      this.info(`Successfully loaded ${count} role${count === 1 ? '' : 's'}.`, 'Roles Loaded');
    }
  }

  noUsersFound() {
    this.info('No users found. Create the first user to get started.', 'No Users');
  }

  usersLoaded(count: number) {
    if (count > 0) {
      this.info(`Successfully loaded ${count} user${count === 1 ? '' : 's'}.`, 'Users Loaded');
    }
  }

  confirmDeleteUser(userName: string) {
    this.warning(`Are you sure you want to delete user "${userName}"? This action cannot be undone.`, 'Confirm Delete');
  }

  // Note: Confirmation dialogs have been moved to DialogService
  // Use DialogService.confirm() or DialogService.simpleConfirm() instead
} 