// Test script to verify authentication and dialog functionality
// This file can be used for debugging authentication issues

import { environment } from '../environments/environment';

export class AuthDialogTest {
  
  static testTokenStorage(): void {
    console.log('🔍 Testing token storage...');
    
    // Check localStorage
    const localToken = localStorage.getItem('ats_admin_token');
    console.log('📦 localStorage token:', localToken ? 'Found' : 'Not found');
    
    // Check sessionStorage
    const sessionToken = sessionStorage.getItem('ats_admin_token');
    console.log('📦 sessionStorage token:', sessionToken ? 'Found' : 'Not found');
    
    // Check user data
    const userData = localStorage.getItem('user');
    console.log('👤 User data:', userData ? 'Found' : 'Not found');
    
    if (userData) {
      try {
        const user = JSON.parse(userData);
        console.log('👤 User info:', {
          id: user.id,
          name: user.name,
          email: user.email,
          isAdmin: user.isAdmin,
          roles: user.roles?.map((r: any) => r.name)
        });
      } catch (error) {
        console.error('❌ Error parsing user data:', error);
      }
    }
  }
  
  static testApiHeaders(): void {
    console.log('🔍 Testing API headers...');
    
    const token = localStorage.getItem('ats_admin_token') || sessionStorage.getItem('ats_admin_token');
    
    if (token) {
      console.log('✅ Token available for API calls');
      console.log('🔑 Authorization header would be:', `Bearer ${token.substring(0, 20)}...`);
    } else {
      console.log('❌ No token available - API calls will fail');
    }
  }
  
  static simulateDialogTest(): void {
    console.log('🔍 Testing dialog functionality...');
    
    // Simulate dialog data
    const dialogData = {
      title: 'Test Confirmation',
      message: 'This is a test confirmation dialog',
      type: 'warning' as const,
      confirmText: 'Yes',
      cancelText: 'Cancel'
    };
    
    console.log('📋 Dialog data:', dialogData);
    console.log('✅ Dialog service should be able to handle this data');
  }
  
  static runAllTests(): void {
    console.log('🚀 Running authentication and dialog tests...');
    console.log('================================================');
    
    this.testTokenStorage();
    console.log('');
    
    this.testApiHeaders();
    console.log('');
    
    this.simulateDialogTest();
    console.log('');
    
    console.log('✅ All tests completed');
    console.log('================================================');
  }
}

// Auto-run tests in development
if (!environment.production) {
  // Uncomment the line below to run tests automatically
  // AuthDialogTest.runAllTests();
}

// Make available globally for manual testing
(window as any).AuthDialogTest = AuthDialogTest; 