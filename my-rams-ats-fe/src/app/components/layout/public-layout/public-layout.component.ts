import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-public-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './public-layout.component.html'
})
export class PublicLayoutComponent implements OnInit {
  showMobileMenu = false;
  showUserMenu = false;
  isAuthenticated = false;
  userName = '';
  userEmail = '';
  userInitials = '';
  dashboardLink = '';
  currentYear = new Date().getFullYear();

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.isAuthenticated = !!user;
      if (user) {
        this.userName = user.name;
        this.userEmail = user.email;
        this.userInitials = this.getInitials(user.name);
        this.dashboardLink = this.authService.isAdmin() ? '/admin/dashboard' : '/user/dashboard';
      }
    });
  }

  toggleMobileMenu(): void {
    this.showMobileMenu = !this.showMobileMenu;
    this.showUserMenu = false;
  }

  closeMobileMenu(): void {
    this.showMobileMenu = false;
  }

  toggleUserMenu(): void {
    this.showUserMenu = !this.showUserMenu;
  }

  logout(): void {
    this.authService.logout().subscribe(() => {
      this.showUserMenu = false;
      this.showMobileMenu = false;
      this.router.navigate(['/']);
    });
  }

  private getInitials(name: string): string {
    return name
      .split(' ')
      .map(part => part.charAt(0))
      .join('')
      .toUpperCase()
      .substring(0, 2);
  }
} 

