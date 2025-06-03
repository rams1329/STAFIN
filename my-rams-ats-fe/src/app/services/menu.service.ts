import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Menu {
  id?: number;
  name: string;
  description?: string;
  icon?: string;
  url: string;
  parentId?: number;
  displayOrder?: number;
  active: boolean;
  canView?: boolean;
  canAdd?: boolean;
  canEdit?: boolean;
  canDelete?: boolean;
  children?: Menu[];
  selectedRoleId?: number | string; // For UI state in orphaned menu assignment
}

export interface Role {
  id: number;
  name: string;
  description?: string;
}

export interface MenuAnalysis {
  userEmail: string;
  userName: string;
  userRoles: string[];
  visibleMenus: Menu[];
  allMenus: Menu[];
  totalMenuCount: number;
  visibleMenuCount: number;
  menusByRole: { [roleName: string]: Menu[] };
  menuCountByRole: { [roleName: string]: number };
  menuPermissions: { [menuName: string]: { [permission: string]: boolean } };
  orphanedMenus: Menu[];
  adminOnlyMenus: Menu[];
  publicMenus: Menu[];
  menuTree: Menu[];
  statistics: { [key: string]: any };
}

export interface MenuRoleAssignmentRequest {
  roleId: number;
  menuIds: number[];
  canView?: boolean;
  canAdd?: boolean;
  canEdit?: boolean;
  canDelete?: boolean;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data?: T;
  error?: string;
}

@Injectable({
  providedIn: 'root'
})
export class MenuService {
  private baseUrl = `${environment.apiUrl}/api/menus`;

  constructor(private http: HttpClient) {}

  // Basic CRUD Operations
  getAllMenus(): Observable<Menu[]> {
    return this.http.get<Menu[]>(this.baseUrl);
  }

  getMenuById(id: number): Observable<Menu> {
    return this.http.get<Menu>(`${this.baseUrl}/${id}`);
  }

  createMenu(menu: Menu): Observable<Menu> {
    return this.http.post<Menu>(this.baseUrl, menu);
  }

  updateMenu(id: number, menu: Menu): Observable<Menu> {
    return this.http.put<Menu>(`${this.baseUrl}/${id}`, menu);
  }

  deleteMenu(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${id}`);
  }

  // Menu Analysis and Insights
  getMenuAnalysis(): Observable<ApiResponse<MenuAnalysis>> {
    return this.http.get<ApiResponse<MenuAnalysis>>(`${this.baseUrl}/analysis`);
  }

  getMenuMappings(): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.baseUrl}/mappings`);
  }

  // Tree and Structure
  getMenuTree(): Observable<ApiResponse<Menu[]>> {
    return this.http.get<ApiResponse<Menu[]>>(`${this.baseUrl}/tree`);
  }

  reorderMenus(menuOrders: { id: number; displayOrder: number }[]): Observable<ApiResponse<string>> {
    return this.http.put<ApiResponse<string>>(`${this.baseUrl}/reorder`, menuOrders);
  }

  // Role Management
  getRoleMenus(roleId: number): Observable<Menu[]> {
    return this.http.get<Menu[]>(`${this.baseUrl}/role/${roleId}`);
  }

  getMenuAvailabilityForRole(roleId: number): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.baseUrl}/availability/role/${roleId}`);
  }

  assignMenuToRole(
    menuId: number, 
    roleId: number, 
    permissions: { canView?: boolean; canAdd?: boolean; canEdit?: boolean; canDelete?: boolean } = {}
  ): Observable<any> {
    const params = new HttpParams()
      .set('canView', permissions.canView?.toString() || 'true')
      .set('canAdd', permissions.canAdd?.toString() || 'false')
      .set('canEdit', permissions.canEdit?.toString() || 'false')
      .set('canDelete', permissions.canDelete?.toString() || 'false');

    return this.http.post(`${this.baseUrl}/${menuId}/role/${roleId}`, {}, { params });
  }

  removeMenuFromRole(menuId: number, roleId: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${menuId}/role/${roleId}`);
  }

  bulkAssignMenusToRole(request: MenuRoleAssignmentRequest): Observable<ApiResponse<string>> {
    return this.http.post<ApiResponse<string>>(`${this.baseUrl}/bulk-assign`, request);
  }

  // Menu Status and Route Management
  toggleMenuStatus(id: number): Observable<ApiResponse<Menu>> {
    return this.http.patch<ApiResponse<Menu>>(`${this.baseUrl}/${id}/toggle-active`, {});
  }

  updateMenuRoute(id: number, newRoute: string): Observable<ApiResponse<Menu>> {
    return this.http.patch<ApiResponse<Menu>>(`${this.baseUrl}/${id}/route`, { url: newRoute });
  }

  // User Menus
  getCurrentUserMenus(): Observable<Menu[]> {
    return this.http.get<Menu[]>(`${this.baseUrl}/user`);
  }

  // Admin Operations
  fixAdminMenuAssignments(): Observable<ApiResponse<string>> {
    return this.http.post<ApiResponse<string>>(`${this.baseUrl}/fix-admin-assignments`, {});
  }

  // Helper Methods
  buildMenuTree(menus: Menu[]): Menu[] {
    const menuMap = new Map<number, Menu>();
    const rootMenus: Menu[] = [];

    // Initialize children arrays and create map
    menus.forEach(menu => {
      menu.children = [];
      if (menu.id) {
        menuMap.set(menu.id, menu);
      }
    });

    // Build tree structure
    menus.forEach(menu => {
      if (menu.parentId && menuMap.has(menu.parentId)) {
        const parent = menuMap.get(menu.parentId)!;
        parent.children!.push(menu);
      } else {
        rootMenus.push(menu);
      }
    });

    return rootMenus;
  }

  // Search and Filter
  filterMenus(menus: Menu[], searchTerm: string, statusFilter: string): Menu[] {
    return menus.filter(menu => {
      const matchesSearch = !searchTerm || 
        menu.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        menu.url.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (menu.description && menu.description.toLowerCase().includes(searchTerm.toLowerCase()));
      
      const matchesStatus = !statusFilter ||
        (statusFilter === 'active' && menu.active) ||
        (statusFilter === 'inactive' && !menu.active);
      
      return matchesSearch && matchesStatus;
    });
  }

  // Validation helpers
  validateMenuForm(menu: Partial<Menu>): string[] {
    const errors: string[] = [];
    
    if (!menu.name || menu.name.trim().length === 0) {
      errors.push('Menu name is required');
    }
    
    if (!menu.url || menu.url.trim().length === 0) {
      errors.push('Menu URL is required');
    } else if (!menu.url.startsWith('/')) {
      errors.push('Menu URL must start with /');
    }
    
    if (menu.displayOrder !== undefined && menu.displayOrder < 0) {
      errors.push('Display order cannot be negative');
    }
    
    return errors;
  }
} 