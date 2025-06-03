import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { UserService, User, AdminCreateUserRequest, Role } from '../../../services/user.service';
import { ToastService } from '../../../services/toast.service';
import { DialogService } from '../../../services/dialog.service';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.css']
})
export class UserManagementComponent implements OnInit {
  userForm: FormGroup;
  allUsers: User[] = [];
  filteredUsers: User[] = [];
  availableRoles: Role[] = [];
  loading = false;
  loadingRoles = false;
  isLoading = false;
  loadingActions: { [key: number]: boolean } = {};
  searchTerm = '';

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private toastService: ToastService,
    private dialogService: DialogService
  ) {
    this.userForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      roleIds: [[], [this.minimumRoleValidator]]
    });
  }

  ngOnInit(): void {
    this.loadRoles();
    this.loadUsers();
  }

  // ========================================
  // ROLE MANAGEMENT
  // ========================================

  loadRoles(): void {
    this.loadingRoles = true;
    this.userService.getAllRoles().subscribe({
      next: (roles) => {
        this.availableRoles = roles;
        this.loadingRoles = false;
        console.log('âœ… Loaded roles:', roles);
      },
      error: (error) => {
        console.error('âŒ Error loading roles:', error);
        this.toastService.error(error.message || 'Failed to load roles');
        this.loadingRoles = false;
      }
    });
  }

  onRoleChange(roleId: number, event: any): void {
    const currentRoleIds = this.userForm.get('roleIds')?.value || [];
    
    if (event.target.checked) {
      // Add role
      if (!currentRoleIds.includes(roleId)) {
        this.userForm.patchValue({
          roleIds: [...currentRoleIds, roleId]
        });
      }
    } else {
      // Remove role
      this.userForm.patchValue({
        roleIds: currentRoleIds.filter((id: number) => id !== roleId)
      });
    }

    // Trigger validation
    this.userForm.get('roleIds')?.updateValueAndValidity();
  }

  // ========================================
  // USER MANAGEMENT
  // ========================================

  loadUsers(): void {
    this.loading = true;
    this.userService.getUsers().subscribe({
      next: (users) => {
        this.allUsers = users;
        this.applyFilters();
        this.loading = false;
        console.log('âœ… Loaded users:', users);
      },
      error: (error) => {
        console.error('âŒ Error loading users:', error);
        this.toastService.error(error.message || 'Failed to load users');
        this.loading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.userForm.valid) {
      this.isLoading = true;
      const formData = this.userForm.value;
      
      const userData: AdminCreateUserRequest = {
        name: formData.name,
        email: formData.email,
        roleIds: formData.roleIds
      };

      console.log('ðŸš€ Creating user:', userData);

      this.userService.createUserByAdmin(userData).subscribe({
        next: (response) => {
          console.log('âœ… User created successfully:', response);
          this.toastService.userCreated(userData.name, true);
          this.resetForm();
          this.loadUsers(); // Reload the user list
          this.isLoading = false;
        },
        error: (error) => {
          console.error('âŒ Error creating user:', error);
          this.isLoading = false;
          
          if (error.message.includes('Email already in use')) {
            this.toastService.emailAlreadyExists();
          } else if (error.message.includes('role')) {
            this.toastService.roleAssignmentFailed();
          } else {
            this.toastService.error(error.message || 'Failed to create user');
          }
        }
      });
    }
  }

  toggleUserStatus(user: User): void {
    this.loadingActions[user.id] = true;
    
    this.userService.toggleUserStatus(user.id).subscribe({
      next: (updatedUser) => {
        console.log('âœ… User status updated:', updatedUser);
        this.toastService.userStatusChanged(user.name, updatedUser.active);
        this.loadUsers(); // Reload to get updated data
        this.loadingActions[user.id] = false;
      },
      error: (error) => {
        console.error('âŒ Error updating user status:', error);
        this.toastService.error(error.message || 'Failed to update user status');
        this.loadingActions[user.id] = false;
      }
    });
  }

  resetPassword(user: User): void {
    this.dialogService.confirmResetPassword(user.name).then((confirmed) => {
      if (confirmed) {
        this.loadingActions[user.id] = true;
        
        this.userService.resetPassword(user.id).subscribe({
          next: () => {
            console.log('âœ… Password reset for user:', user.name);
            this.toastService.passwordReset(user.email);
            this.loadingActions[user.id] = false;
          },
          error: (error) => {
            console.error('âŒ Error resetting password:', error);
            this.toastService.error(error.message || 'Failed to reset password');
            this.loadingActions[user.id] = false;
          }
        });
      }
    });
  }

  deleteUser(user: User): void {
    this.dialogService.confirmDeleteUser(user.name).then((confirmed) => {
      if (confirmed) {
        this.loadingActions[user.id] = true;
        
        this.userService.deleteUser(user.id).subscribe({
          next: () => {
            console.log('âœ… User deleted:', user.name);
            this.toastService.userDeleted(user.name);
            this.loadUsers(); // Reload the user list
            this.loadingActions[user.id] = false;
          },
          error: (error) => {
            console.error('âŒ Error deleting user:', error);
            this.toastService.error(error.message || 'Failed to delete user');
            this.loadingActions[user.id] = false;
          }
        });
      }
    });
  }

  // ========================================
  // UTILITY METHODS
  // ========================================

  applyFilters(): void {
    if (!this.searchTerm.trim()) {
      this.filteredUsers = [...this.allUsers];
    } else {
      const searchLower = this.searchTerm.toLowerCase();
      this.filteredUsers = this.allUsers.filter(user => 
        user.name.toLowerCase().includes(searchLower) ||
        user.email.toLowerCase().includes(searchLower) ||
        user.roles?.some(role => role.name.toLowerCase().includes(searchLower))
      );
    }
  }

  resetForm(): void {
    this.userForm.reset({
      name: '',
      email: '',
      roleIds: []
    });

    // Uncheck all role checkboxes
    this.availableRoles.forEach(role => {
      const checkbox = document.getElementById(`role-${role.id}`) as HTMLInputElement;
      if (checkbox) {
        checkbox.checked = false;
      }
    });
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.userForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  minimumRoleValidator(control: AbstractControl): ValidationErrors | null {
    const roles = control.value;
    return roles && roles.length > 0 ? null : { minimumRole: true };
  }
} 

