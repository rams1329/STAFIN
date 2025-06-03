import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { User } from '../../../models/user.model';
import { ContactService } from '../../../services/contact.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar.component.html'
})
export class NavbarComponent implements OnInit {
  currentUser: User | null = null;
  unreadMessages = 0;

  constructor(
    private authService: AuthService,
    private contactService: ContactService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Subscribe to current user
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });

    // Load unread messages count
    this.loadUnreadCount();
  }

  getInitials(name?: string): string {
    if (!name) return 'U';
    return name.split(' ')
      .map(word => word.charAt(0))
      .join('')
      .toUpperCase()
      .slice(0, 2);
  }

  navigateToProfile(): void {
    this.router.navigate(['/admin/profile']);
  }

  navigateToSettings(): void {
    this.router.navigate(['/admin/settings']);
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/login']);
      },
      error: (error) => {
        console.error('Logout error:', error);
        // Still navigate to login even if API call fails
        this.router.navigate(['/login']);
      }
    });
  }

  private loadUnreadCount(): void {
    this.contactService.getUnreadCount().subscribe({
      next: (response: {count: number}) => {
        this.unreadMessages = response.count;
      },
      error: (error: any) => {
        console.error('Error loading unread count:', error);
      }
    });
  }
} 

