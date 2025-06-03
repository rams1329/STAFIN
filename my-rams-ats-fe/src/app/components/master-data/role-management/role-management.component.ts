import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { UserService, Role } from '../../../services/user.service';
import { ToastService } from '../../../services/toast.service';

@Component({
  selector: 'app-role-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './role-management.component.html'
})
export class RoleManagementComponent implements OnInit {
  allRoles: Role[] = [];
  filteredRoles: Role[] = [];
  loading = false;
  searchTerm = '';
  selectedRole: Role | null = null;

  constructor(
    private userService: UserService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.loadRoles();
  }

  // ========================================
  // ROLE MANAGEMENT
  // ========================================

  loadRoles(): void {
    this.loading = true;
    this.userService.getAllRoles().subscribe({
      next: (roles) => {
        this.allRoles = roles;
        this.applyFilters();
        this.loading = false;
        console.log('âœ… Loaded roles:', roles);
        
        if (roles.length > 0) {
          this.toastService.rolesLoaded(roles.length);
        }
      },
      error: (error) => {
        console.error('âŒ Error loading roles:', error);
        this.toastService.error(error.message || 'Failed to load roles');
        this.loading = false;
      }
    });
  }

  viewRoleDetails(role: Role): void {
    this.selectedRole = role;
  }

  closeRoleDetails(): void {
    this.selectedRole = null;
  }

  // ========================================
  // UTILITY METHODS
  // ========================================

  applyFilters(): void {
    if (!this.searchTerm.trim()) {
      this.filteredRoles = [...this.allRoles];
    } else {
      const searchLower = this.searchTerm.toLowerCase();
      this.filteredRoles = this.allRoles.filter(role => 
        role.name.toLowerCase().includes(searchLower) ||
        (role.description && role.description.toLowerCase().includes(searchLower))
      );
    }
  }
} 

