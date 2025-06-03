import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { ContactService } from '../../../services/contact.service';
import { MenuService, Menu } from '../../../services/menu.service';
import { AuthService } from '../../../services/auth.service';
import { User } from '../../../models/user.model';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {
  showMasterData = false;
  unreadCount = 0;
  currentUser: User | null = null;
  dynamicMenus: Menu[] = [];
  loadingMenus = true;
  openDynamicMenus: Set<number> = new Set();

  constructor(
    private contactService: ContactService,
    private router: Router,
    private menuService: MenuService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadUnreadCount();
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      if (user) {
        this.loadDynamicMenus();
      }
    });

    // Listen for menu updates
    window.addEventListener('menuUpdated', () => {
      this.loadDynamicMenus();
    });
  }

  toggleMasterData(): void {
    // Only allow toggle on desktop screens
    if (window.innerWidth > 768) {
      this.showMasterData = !this.showMasterData;
    }
  }

  private loadUnreadCount(): void {
    this.contactService.getUnreadCount().subscribe({
      next: (response: {count: number}) => {
        this.unreadCount = response.count;
      },
      error: (error: any) => {
        console.error('Error loading unread count:', error);
        // Set to 0 if API fails
        this.unreadCount = 0;
      }
    });
  }

  private loadDynamicMenus(): void {
    this.loadingMenus = true;
    this.menuService.getCurrentUserMenus().subscribe({
      next: (menus: Menu[]) => {
        // Filter out menus that don't have user permissions or are inactive
        this.dynamicMenus = menus.filter(menu => 
          menu.active && 
          (menu.canView === true) &&
          // Exclude system/admin menus that are already hardcoded
          !this.isSystemMenu(menu.url)
        );
        
        // Build the tree structure
        this.buildMenuTree();
        this.loadingMenus = false;
      },
      error: (error: any) => {
        console.error('Error loading dynamic menus:', error);
        this.dynamicMenus = [];
        this.loadingMenus = false;
      }
    });
  }

  private isSystemMenu(url: string): boolean {
    const systemUrls = [
      '/admin/dashboard',
      '/admin/jobs',
      '/admin/jobs/create',
      '/admin/users',
      '/admin/contact-messages',
      '/admin/master-data/departments',
      '/admin/master-data/job-functions',
      '/admin/master-data/job-types',
      '/admin/master-data/locations',
      '/admin/master-data/experience-levels',
      '/admin/master-data/roles',
      '/admin/master-data/menus'
    ];
    return systemUrls.includes(url);
  }

  private buildMenuTree(): void {
    // Build parent-child relationships
    this.dynamicMenus.forEach(menu => {
      if (menu.parentId) {
        const parent = this.dynamicMenus.find(m => m.id === menu.parentId);
        if (parent) {
          if (!parent.children) {
            parent.children = [];
          }
          parent.children.push(menu);
        }
      }
    });

    // Sort menus by display order
    this.dynamicMenus.sort((a, b) => (a.displayOrder || 0) - (b.displayOrder || 0));
    this.dynamicMenus.forEach(menu => {
      if (menu.children) {
        menu.children.sort((a, b) => (a.displayOrder || 0) - (b.displayOrder || 0));
      }
    });
  }

  toggleDynamicMenu(menuId: number): void {
    if (this.openDynamicMenus.has(menuId)) {
      this.openDynamicMenus.delete(menuId);
    } else {
      this.openDynamicMenus.add(menuId);
    }
  }

  getTopLevelMenus(): Menu[] {
    return this.dynamicMenus.filter(menu => !menu.parentId);
  }

  hasChildren(menu: Menu): boolean {
    return !!(menu.children && menu.children.length > 0);
  }

  isDynamicMenuOpen(menuId: number): boolean {
    return this.openDynamicMenus.has(menuId);
  }

  // Refresh dynamic menus (can be called when menus are updated)
  refreshMenus(): void {
    this.loadDynamicMenus();
  }
} 

