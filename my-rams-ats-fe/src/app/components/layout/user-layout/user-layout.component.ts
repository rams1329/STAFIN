import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { MenuService, Menu } from '../../../services/menu.service';

@Component({
  selector: 'app-user-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './user-layout.component.html'
})
export class UserLayoutComponent implements OnInit {
  userName = '';
  userMenus: Menu[] = [];
  loadingMenus = true;

  constructor(
    private authService: AuthService,
    private router: Router,
    private menuService: MenuService
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      if (user) {
        this.userName = user.name;
        this.loadUserMenus();
      }
    });

    // Listen for menu updates
    window.addEventListener('menuUpdated', () => {
      this.loadUserMenus();
    });
  }

  private loadUserMenus(): void {
    this.loadingMenus = true;
    this.menuService.getCurrentUserMenus().subscribe({
      next: (menus: Menu[]) => {
        // Filter menus for user access and exclude system menus
        this.userMenus = menus.filter(menu => 
          menu.active && 
          (menu.canView === true) &&
          !this.isSystemMenu(menu.url)
        );
        
        // Build tree structure
        this.buildMenuTree();
        this.loadingMenus = false;
      },
      error: (error: any) => {
        console.error('Error loading user menus:', error);
        this.userMenus = [];
        this.loadingMenus = false;
      }
    });
  }

  private isSystemMenu(url: string): boolean {
    const userSystemUrls = [
      '/user/dashboard',
      '/user/jobs',
      '/user/profile'
    ];
    return userSystemUrls.includes(url);
  }

  private buildMenuTree(): void {
    // Build parent-child relationships
    this.userMenus.forEach(menu => {
      if (menu.parentId) {
        const parent = this.userMenus.find(m => m.id === menu.parentId);
        if (parent) {
          if (!parent.children) {
            parent.children = [];
          }
          parent.children.push(menu);
        }
      }
    });

    // Sort menus by display order
    this.userMenus.sort((a, b) => (a.displayOrder || 0) - (b.displayOrder || 0));
    this.userMenus.forEach(menu => {
      if (menu.children) {
        menu.children.sort((a, b) => (a.displayOrder || 0) - (b.displayOrder || 0));
      }
    });

    // Only keep top-level menus for navigation
    this.userMenus = this.userMenus.filter(menu => !menu.parentId);
  }

  hasChildren(menu: Menu): boolean {
    return !!(menu.children && menu.children.length > 0);
  }

  logout(): void {
    this.authService.logout().subscribe(() => {
      this.router.navigate(['/']);
    });
  }
} 

