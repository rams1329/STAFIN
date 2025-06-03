import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ToastService } from '../../../services/toast.service';
import { MenuService, Menu, Role, MenuAnalysis, MenuRoleAssignmentRequest } from '../../../services/menu.service';
import { UserService } from '../../../services/user.service';

@Component({
  selector: 'app-menu-management',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  styleUrl: './menu-management.component.css',
  templateUrl: './menu-management.component.html'
})
export class MenuManagementComponent implements OnInit {
  menus: Menu[] = [];
  filteredMenus: Menu[] = [];
  roles: Role[] = [];
  menuTree: Menu[] = [];
  analysis?: MenuAnalysis;
  
  // UI State
  showModal = false;
  showRouteModal = false;
  showPermissionModal = false;
  isEditing = false;
  showAnalysis = false;
  activeTab = 'list';
  searchTerm = '';
  statusFilter = '';
  selectedRoleId: number | string = '';
  roleMenuData: any = null;
  loading = false;
  
  // Bulk operations
  bulkMode = false;
  selectedMenuIds: number[] = [];
  bulkTargetRoleId: number | string = '';
  
  // Modals
  selectedMenuForRoute?: Menu;
  selectedMenuForPermission?: any;
  
  tabs = [
    { id: 'list', label: 'Menu List', icon: 'fas fa-list' },
    { id: 'roles', label: 'Role Management', icon: 'fas fa-users' },
    { id: 'tree', label: 'Tree View', icon: 'fas fa-sitemap' },
    { id: 'orphaned', label: 'Orphaned Menus', icon: 'fas fa-exclamation-triangle' }
  ];

  menuForm: FormGroup;
  routeForm: FormGroup;
  permissionForm: FormGroup;

  constructor(
    private menuService: MenuService,
    private userService: UserService,
    private fb: FormBuilder,
    private toastService: ToastService
  ) {
    this.menuForm = this.fb.group({
      id: [null],
      name: ['', Validators.required],
      description: [''],
      icon: [''],
      url: ['', Validators.required],
      parentId: [null],
      displayOrder: [0],
      active: [true]
    });

    this.routeForm = this.fb.group({
      url: ['', [Validators.required, Validators.pattern(/^\/.*/)]]
    });

    this.permissionForm = this.fb.group({
      canView: [true],
      canAdd: [false],
      canEdit: [false],
      canDelete: [false]
    });
  }

  ngOnInit(): void {
    this.loadMenus();
    this.loadRoles();
    this.loadMenuTree();
    this.loadAnalysis();
  }

  loadMenus(): void {
    this.menuService.getAllMenus().subscribe({
      next: (data) => {
        this.menus = data;
        this.filterMenus();
      },
      error: (error) => this.toastService.error('Failed to load menus: ' + error.message)
    });
  }

  loadRoles(): void {
    this.userService.getAllRoles().subscribe({
      next: (data) => this.roles = data,
      error: (error) => this.toastService.error('Failed to load roles: ' + error.message)
    });
  }

  loadMenuTree(): void {
    this.menuService.getMenuTree().subscribe({
      next: (response) => this.menuTree = response.data || [],
      error: (error) => this.toastService.error('Failed to load menu tree: ' + error.message)
    });
  }

  loadAnalysis(): void {
    this.menuService.getMenuAnalysis().subscribe({
      next: (response) => this.analysis = response.data,
      error: (error) => this.toastService.error('Failed to load analysis: ' + error.message)
    });
  }

  loadRoleMenus(): void {
    if (!this.selectedRoleId) return;
    
    this.menuService.getMenuAvailabilityForRole(+this.selectedRoleId).subscribe({
      next: (response) => this.roleMenuData = response.data,
      error: (error) => this.toastService.error('Failed to load role menus: ' + error.message)
    });
  }

  filterMenus(): void {
    this.filteredMenus = this.menuService.filterMenus(this.menus, this.searchTerm, this.statusFilter);
  }

  openCreateModal(): void {
    this.isEditing = false;
    this.menuForm.reset({ active: true, displayOrder: 0 });
    this.showModal = true;
  }

  editMenu(menu: Menu): void {
    this.isEditing = true;
    this.menuForm.patchValue(menu);
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.menuForm.reset();
  }

  saveMenu(): void {
    if (this.menuForm.invalid) return;

    this.loading = true;
    const menuData = this.menuForm.value;
    
    const request = this.isEditing 
      ? this.menuService.updateMenu(menuData.id, menuData)
      : this.menuService.createMenu(menuData);

    request.subscribe({
      next: () => {
        this.toastService.success(
          this.isEditing ? 'Menu updated successfully' : 'Menu created successfully'
        );
        this.closeModal();
        this.loadMenus();
        this.loadMenuTree();
        this.loadAnalysis();
        this.loading = false;
        
        // Notify that menus have been updated
        window.dispatchEvent(new CustomEvent('menuUpdated'));
      },
      error: (error) => {
        this.toastService.error('Failed to save menu: ' + error.message);
        this.loading = false;
      }
    });
  }

  deleteMenu(menu: Menu): void {
    if (confirm(`Are you sure you want to delete "${menu.name}"?`)) {
      this.menuService.deleteMenu(menu.id!).subscribe({
        next: () => {
          this.toastService.success('Menu deleted successfully');
          this.loadMenus();
          this.loadMenuTree();
          this.loadAnalysis();
          
          // Notify that menus have been updated
          window.dispatchEvent(new CustomEvent('menuUpdated'));
        },
        error: (error) => this.toastService.error('Failed to delete menu: ' + error.message)
      });
    }
  }

  toggleMenuStatus(menu: Menu): void {
    this.menuService.toggleMenuStatus(menu.id!).subscribe({
      next: (response) => {
        menu.active = response.data!.active;
        this.toastService.success(`Menu ${menu.active ? 'activated' : 'deactivated'} successfully`);
        this.filterMenus();
      },
      error: (error) => this.toastService.error('Failed to toggle menu status: ' + error.message)
    });
  }

  assignMenuToRole(menuId: number, roleId: number | string): void {
    this.menuService.assignMenuToRole(menuId, +roleId).subscribe({
      next: () => {
        this.toastService.success('Menu assigned to role successfully');
        this.loadRoleMenus();
        this.loadAnalysis();
      },
      error: (error) => this.toastService.error('Failed to assign menu: ' + error.message)
    });
  }

  removeMenuFromRole(menuId: number, roleId: number | string): void {
    this.menuService.removeMenuFromRole(menuId, +roleId).subscribe({
      next: () => {
        this.toastService.success('Menu removed from role successfully');
        this.loadRoleMenus();
        this.loadAnalysis();
      },
      error: (error) => this.toastService.error('Failed to remove menu: ' + error.message)
    });
  }

  assignOrphanedMenu(menu: any): void {
    if (!menu.selectedRoleId) return;
    this.assignMenuToRole(menu.id, menu.selectedRoleId);
  }

  manageRoles(menu: Menu): void {
    // Switch to roles tab and set the menu context
    this.activeTab = 'roles';
    // Could implement a more sophisticated role management modal here
  }

  getMenuRoles(menu: Menu): string[] {
    // This would need to be populated from the menu mappings data
    // For now, return empty array as placeholder
    return [];
  }

  getRoleMenuEntries(): { key: string, value: Menu[] }[] {
    if (!this.analysis) return [];
    return Object.entries(this.analysis.menusByRole).map(([key, value]) => ({ key, value }));
  }

  getCoveragePercentage(): number {
    if (!this.analysis) return 0;
    return Math.round((this.analysis.visibleMenuCount / this.analysis.totalMenuCount) * 100);
  }

  getAssignmentPercentage(roleMenuData: any): number {
    if (!roleMenuData || !roleMenuData.totalMenus || !roleMenuData.assignedMenus) return 0;
    return Math.round((roleMenuData.assignedMenus / roleMenuData.totalMenus) * 100);
  }

  getOrphanedMenus(): Menu[] {
    if (!this.analysis) return [];
    return this.analysis.orphanedMenus;
  }

  // Bulk Operations
  toggleBulkMode(): void {
    this.bulkMode = !this.bulkMode;
    this.selectedMenuIds = [];
    this.bulkTargetRoleId = '';
  }

  allMenusSelected(): boolean {
    return this.filteredMenus.length > 0 && 
           this.filteredMenus.every(menu => this.selectedMenuIds.includes(menu.id!));
  }

  isMenuSelected(menuId: number): boolean {
    return this.selectedMenuIds.includes(menuId);
  }

  toggleAllMenus(event: Event): void {
    const target = event.target as HTMLInputElement;
    if (target.checked) {
      this.selectedMenuIds = [...this.filteredMenus.map(menu => menu.id!)];
    } else {
      this.selectedMenuIds = [];
    }
  }

  toggleMenuSelection(menuId: number, event: Event): void {
    const target = event.target as HTMLInputElement;
    if (target.checked) {
      if (!this.selectedMenuIds.includes(menuId)) {
        this.selectedMenuIds.push(menuId);
      }
    } else {
      this.selectedMenuIds = this.selectedMenuIds.filter(id => id !== menuId);
    }
  }

  bulkAssignToRole(): void {
    if (!this.bulkTargetRoleId || this.selectedMenuIds.length === 0) return;

    const request: MenuRoleAssignmentRequest = {
      roleId: +this.bulkTargetRoleId,
      menuIds: this.selectedMenuIds,
      canView: true,
      canAdd: false,
      canEdit: false,
      canDelete: false
    };

    this.menuService.bulkAssignMenusToRole(request).subscribe({
      next: (response) => {
        this.toastService.success(response.message || 'Bulk assignment completed');
        this.clearSelection();
        this.loadAnalysis();
        this.loadRoleMenus();
      },
      error: (error) => this.toastService.error('Failed to bulk assign menus: ' + error.message)
    });
  }

  clearSelection(): void {
    this.selectedMenuIds = [];
    this.bulkTargetRoleId = '';
    this.bulkMode = false;
  }

  // Route Management
  editRoute(menu: Menu): void {
    this.selectedMenuForRoute = menu;
    this.routeForm.patchValue({ url: menu.url });
    this.showRouteModal = true;
  }

  saveRoute(): void {
    if (this.routeForm.invalid || !this.selectedMenuForRoute) return;

    this.loading = true;
    const newRoute = this.routeForm.get('url')?.value;

    this.menuService.updateMenuRoute(this.selectedMenuForRoute.id!, newRoute).subscribe({
      next: (response) => {
        this.toastService.success('Menu route updated successfully');
        this.closeRouteModal();
        this.loadMenus();
        this.loadMenuTree();
        this.loadAnalysis();
        this.loading = false;
        
        // Notify that menus have been updated
        window.dispatchEvent(new CustomEvent('menuUpdated'));
      },
      error: (error) => {
        this.toastService.error('Failed to update menu route: ' + error.message);
        this.loading = false;
      }
    });
  }

  closeRouteModal(): void {
    this.showRouteModal = false;
    this.routeForm.reset();
    this.selectedMenuForRoute = undefined;
  }

  // Permission Management
  editPermissions(menuItem: any, roleId: number | string): void {
    this.selectedMenuForPermission = { ...menuItem, roleId };
    this.permissionForm.patchValue({
      canView: menuItem.canView,
      canAdd: menuItem.canAdd,
      canEdit: menuItem.canEdit,
      canDelete: menuItem.canDelete
    });
    this.showPermissionModal = true;
  }

  savePermissions(): void {
    if (!this.selectedMenuForPermission) return;

    this.loading = true;
    const permissions = this.permissionForm.value;

    this.menuService.assignMenuToRole(
      this.selectedMenuForPermission.menuId,
      +this.selectedMenuForPermission.roleId,
      permissions
    ).subscribe({
      next: () => {
        this.toastService.success('Menu permissions updated successfully');
        this.closePermissionModal();
        this.loadRoleMenus();
        this.loadAnalysis();
        this.loading = false;
      },
      error: (error) => {
        this.toastService.error('Failed to update menu permissions: ' + error.message);
        this.loading = false;
      }
    });
  }

  closePermissionModal(): void {
    this.showPermissionModal = false;
    this.permissionForm.reset();
    this.selectedMenuForPermission = undefined;
  }

  getCurrentRoleName(): string {
    const role = this.roles.find(r => r.id === +this.selectedRoleId);
    return role ? role.name : '';
  }

  // Admin Operations
  fixAdminAssignments(): void {
    if (confirm('This will assign all menus to the admin role with full permissions. Continue?')) {
      this.menuService.fixAdminMenuAssignments().subscribe({
        next: (response) => {
          this.toastService.success(response.message || 'Admin menu assignments fixed');
          this.loadAnalysis();
          this.loadRoleMenus();
        },
        error: (error) => this.toastService.error('Failed to fix admin assignments: ' + error.message)
      });
    }
  }

  getAvailableParentMenus(): Menu[] {
    return this.menus.filter(menu => menu.id !== this.menuForm.get('id')?.value);
  }
} 

