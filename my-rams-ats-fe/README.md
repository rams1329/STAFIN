# 🚀 Hyundai Autoever ATS Admin Panel

A comprehensive Angular-based admin panel for Hyundai Autoever's Applicant Tracking System (ATS) with modern UI/UX design patterns inspired by healthcare management systems.

## 📋 Project Overview

This admin panel provides complete management functionality for:
- **Job Postings** - Create, edit, and manage job listings
- **User Management** - Admin user accounts and role management
- **Contact Messages** - Handle inquiries and communications
- **Master Data** - Manage departments, job functions, locations, etc.
- **Dashboard Analytics** - Key metrics and insights

## 🎨 Design System

### **Color Palette (Exact from Reference)**
```scss
--primary-color: #5f6FFF;        // Main brand color
--primary-light: #F2F3FF;        // Light background for active states
--text-gray: #515151;            // Sidebar text color
--border-gray: #9CA3AF;          // Border colors
--success-green: #10B981;        // Success states
--error-red: #EF4444;            // Error states
--warning-yellow: #F59E0B;       // Warning states
```

### **Typography**
- **Font Family**: Outfit (Google Fonts)
- **Responsive text scales** with mobile-first approach
- **Consistent spacing** and visual hierarchy

### **Component Patterns**
- **Cards**: Rounded corners with subtle shadows and hover effects
- **Buttons**: Primary (filled) and Secondary (outlined) variants
- **Forms**: Clean inputs with focus states and validation
- **Tables**: Responsive with hover states and action buttons
- **Sidebar**: Collapsible navigation with active states

## 🏗️ Architecture

### **Technology Stack**
- **Angular 20** - Latest version with standalone components
- **Tailwind CSS** - Utility-first CSS framework
- **TypeScript** - Type-safe development
- **RxJS** - Reactive programming
- **Angular Router** - Client-side routing
- **Angular Forms** - Reactive forms with validation

### **Project Structure**
```
src/
├── app/
│   ├── components/
│   │   ├── auth/
│   │   │   └── login/                 # Login component
│   │   ├── layout/
│   │   │   ├── navbar/                # Top navigation
│   │   │   ├── sidebar/               # Side navigation
│   │   │   └── admin-layout/          # Main layout wrapper
│   │   ├── dashboard/                 # Dashboard with stats
│   │   └── jobs/
│   │       └── job-form/              # Job creation form
│   ├── models/                        # TypeScript interfaces
│   │   ├── user.model.ts
│   │   ├── job.model.ts
│   │   ├── contact.model.ts
│   │   └── master-data.model.ts
│   ├── services/                      # API services
│   │   ├── auth.service.ts
│   │   └── contact.service.ts
│   ├── guards/
│   │   └── auth.guard.ts              # Route protection
│   └── interceptors/
│       └── auth.interceptor.ts        # JWT token handling
├── environments/                      # Environment configs
└── styles.css                        # Global styles
```

## 🔐 Authentication & Security

### **JWT Token Management**
- Automatic token attachment to API requests
- Token expiration handling with auto-logout
- Secure token storage in localStorage
- Role-based access control

### **Route Protection**
- AuthGuard for admin routes
- Automatic redirect to login for unauthenticated users
- Role-based route access

## 📱 Features Implemented

### **✅ Completed Components**

#### **1. Login Page**
- Email/password authentication
- Form validation with error messages
- Remember me functionality
- Responsive design
- Loading states and success/error feedback

#### **2. Admin Layout**
- **Navbar**: User dropdown, notifications, logout
- **Sidebar**: Collapsible navigation with active states
- **Responsive**: Mobile-friendly with proper breakpoints

#### **3. Dashboard**
- **Hero Section**: Welcome message with brand colors
- **Stats Cards**: KPI metrics with icons and trend indicators
- **Charts Placeholder**: Ready for Chart.js integration
- **Recent Activity Table**: Job postings with status indicators

#### **4. Job Management**
- **Add Job Form**: Complete form with rich text editor toolbar
- **Form Validation**: Required fields and data validation
- **Responsive Layout**: Mobile-optimized form design

### **🔄 API Integration Ready**

All services are structured to connect with the backend APIs:

```typescript
// Example API endpoints configured
POST /api/auth/login
GET /api/auth/profile
GET /api/admin/jobs
POST /api/admin/jobs
GET /api/contact/messages/count-unread
```

## 🎯 Key Features

### **Design Consistency**
- **Exact color matching** from reference healthcare project
- **Consistent spacing** and typography scales
- **Hover effects** and smooth transitions
- **Professional enterprise** look and feel

### **User Experience**
- **Intuitive navigation** with clear visual hierarchy
- **Responsive design** for all device sizes
- **Loading states** and user feedback
- **Form validation** with helpful error messages

### **Performance**
- **Standalone components** for better tree-shaking
- **Lazy loading** ready for route-based code splitting
- **Optimized bundle** with Angular 20 features

## 🚀 Getting Started

### **Prerequisites**
- Node.js 18+ 
- Angular CLI 20+
- npm or yarn

### **Installation**
```bash
# Clone the repository
git clone <repository-url>
cd hyundai-autoever-ats

# Install dependencies
npm install

# Start development server
npm start

# Build for production
npm run build
```

### **Development Server**
```bash
ng serve
# Navigate to http://localhost:4200
```

## 🔧 Configuration

### **Environment Variables**
```typescript
// src/environments/environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080',
  tokenKey: 'ats_admin_token',
  // ... other config
};
```

### **Tailwind Configuration**
Custom configuration in `tailwind.config.js` with:
- Brand colors from reference project
- Custom animations and transitions
- Responsive breakpoints
- Component utilities

## 📋 TODO: Next Steps

### **High Priority**
1. **Job Management**
   - Job listing page with search/filter
   - Job details view
   - Edit job functionality
   - Job status management

2. **User Management**
   - User listing with role filters
   - Create/edit user forms
   - Role assignment interface
   - User profile management

3. **Contact Messages**
   - Message inbox with read/unread status
   - Message details view
   - Mark as read functionality
   - Message filtering and search

### **Medium Priority**
4. **Master Data Management**
   - CRUD operations for departments
   - Job functions management
   - Locations and experience levels
   - Role management interface

5. **Dashboard Enhancements**
   - Real API integration
   - Chart.js implementation
   - Real-time updates
   - Export functionality

### **Low Priority**
6. **Advanced Features**
   - Bulk operations
   - Advanced filtering
   - Data export (PDF, Excel)
   - Email notifications
   - Audit logs

## 🎨 Design Reference

This project follows the exact design patterns from a reference healthcare management system:
- **Color scheme**: Primary blue (#5f6FFF) with supporting colors
- **Typography**: Outfit font family with responsive scales
- **Component styling**: Cards, buttons, forms, and tables
- **Layout patterns**: Sidebar navigation with top navbar
- **Interactive elements**: Hover states, transitions, and animations

## 🤝 Contributing

1. Follow the established component patterns
2. Maintain design consistency with reference styles
3. Use TypeScript interfaces for all data models
4. Implement proper error handling and loading states
5. Write responsive, mobile-first CSS

## 📄 License

This project is proprietary to Hyundai Autoever.

---

**Built with ❤️ for Hyundai Autoever ATS**
