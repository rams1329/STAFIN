export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080',
  apiVersion: 'v1',
  appName: 'Hyundai Autoever ATS Admin',
  tokenKey: 'ats_admin_token',
  refreshTokenKey: 'ats_admin_refresh_token',
  defaultPageSize: 10,
  maxFileUploadSize: 5242880, // 5MB in bytes
  supportedImageTypes: ['image/jpeg', 'image/png', 'image/gif'],
  pagination: {
    defaultPageSize: 10,
    pageSizeOptions: [5, 10, 25, 50, 100]
  },
  charts: {
    defaultColors: ['#5f6FFF', '#10B981', '#F59E0B', '#EF4444', '#8B5CF6'],
    backgroundColor: 'rgba(95, 111, 255, 0.1)'
  }
}; 