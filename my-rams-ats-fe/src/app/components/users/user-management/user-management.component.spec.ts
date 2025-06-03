import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { UserManagementComponent } from './user-management.component';
import { UserService, User, Role } from '../../../services/user.service';
import { ToastService } from '../../../services/toast.service';
import { DialogService } from '../../../services/dialog.service';

describe('UserManagementComponent', () => {
  let component: UserManagementComponent;
  let fixture: ComponentFixture<UserManagementComponent>;
  let mockUserService: jasmine.SpyObj<UserService>;
  let mockToastService: jasmine.SpyObj<ToastService>;
  let mockDialogService: jasmine.SpyObj<DialogService>;

  const mockUsers: User[] = [
    {
      id: 1,
      name: 'John Doe',
      email: 'john@example.com',
      active: true,
      roles: [{ id: 1, name: 'Admin', description: 'Administrator' }],
      createdDate: new Date('2023-01-01'),
      lastLoginDate: new Date('2023-01-15'),
      firstLogin: false
    },
    {
      id: 2,
      name: 'Jane Smith',
      email: 'jane@example.com',
      active: false,
      roles: [{ id: 2, name: 'User', description: 'Regular User' }],
      createdDate: new Date('2023-01-02'),
      lastLoginDate: undefined,
      firstLogin: true
    }
  ];

  const mockRoles: Role[] = [
    { id: 1, name: 'Admin', description: 'Administrator' },
    { id: 2, name: 'User', description: 'Regular User' },
    { id: 3, name: 'Manager', description: 'Manager Role' }
  ];

  beforeEach(async () => {
    const userServiceSpy = jasmine.createSpyObj('UserService', [
      'getUsers', 'getAllRoles', 'adminCreateUser', 'toggleUserStatus', 
      'resetPassword', 'deleteUser'
    ]);
    const toastServiceSpy = jasmine.createSpyObj('ToastService', [
      'success', 'error', 'validationError'
    ]);
    const dialogServiceSpy = jasmine.createSpyObj('DialogService', [
      'confirm'
    ]);

    await TestBed.configureTestingModule({
      imports: [UserManagementComponent, ReactiveFormsModule, FormsModule],
      providers: [
        { provide: UserService, useValue: userServiceSpy },
        { provide: ToastService, useValue: toastServiceSpy },
        { provide: DialogService, useValue: dialogServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(UserManagementComponent);
    component = fixture.componentInstance;
    mockUserService = TestBed.inject(UserService) as jasmine.SpyObj<UserService>;
    mockToastService = TestBed.inject(ToastService) as jasmine.SpyObj<ToastService>;
    mockDialogService = TestBed.inject(DialogService) as jasmine.SpyObj<DialogService>;

    // Setup default mock returns
    mockUserService.getUsers.and.returnValue(of(mockUsers));
    mockUserService.getAllRoles.and.returnValue(of(mockRoles));
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form and load data on init', () => {
    component.ngOnInit();

    expect(component.userForm).toBeDefined();
    expect(component.userForm.get('name')).toBeTruthy();
    expect(component.userForm.get('email')).toBeTruthy();
    expect(component.userForm.get('roleIds')).toBeTruthy();
    expect(mockUserService.getAllRoles).toHaveBeenCalled();
    expect(mockUserService.getUsers).toHaveBeenCalled();
  });

  it('should load roles successfully', () => {
    component.loadRoles();

    expect(component.loadingRoles).toBeFalsy();
    expect(component.availableRoles).toEqual(mockRoles);
  });

  it('should handle role loading error', () => {
    mockUserService.getAllRoles.and.returnValue(throwError(() => new Error('Failed to load roles')));

    component.loadRoles();

    expect(component.loadingRoles).toBeFalsy();
    expect(mockToastService.error).toHaveBeenCalledWith('Failed to load roles');
  });

  it('should load users successfully', () => {
    component.loadUsers();

    expect(component.loading).toBeFalsy();
    expect(component.allUsers).toEqual(mockUsers);
    expect(component.filteredUsers).toEqual(mockUsers);
  });

  it('should handle user loading error', () => {
    mockUserService.getUsers.and.returnValue(throwError(() => new Error('Failed to load users')));

    component.loadUsers();

    expect(component.loading).toBeFalsy();
    expect(mockToastService.error).toHaveBeenCalledWith('Failed to load users');
  });

  it('should handle role selection', () => {
    const event = { target: { checked: true } };
    component.onRoleChange(1, event);

    const roleIds = component.userForm.get('roleIds')?.value;
    expect(roleIds).toContain(1);
  });

  it('should handle role deselection', () => {
    component.userForm.patchValue({ roleIds: [1, 2] });
    const event = { target: { checked: false } };
    
    component.onRoleChange(1, event);

    const roleIds = component.userForm.get('roleIds')?.value;
    expect(roleIds).not.toContain(1);
    expect(roleIds).toContain(2);
  });

  it('should create user successfully', () => {
    component.userForm.patchValue({
      name: 'New User',
      email: 'newuser@example.com',
      roleIds: [1]
    });
    const mockUser: User = {
      id: 3,
      name: 'New User',
      email: 'newuser@example.com',
      active: true,
      roles: [{ id: 1, name: 'Admin', description: 'Administrator' }],
      createdDate: new Date(),
      lastLoginDate: undefined,
      firstLogin: true
    };
    mockUserService.adminCreateUser.and.returnValue(of(mockUser));

    component.onSubmit();

    expect(mockUserService.adminCreateUser).toHaveBeenCalledWith({
      name: 'New User',
      email: 'newuser@example.com',
      roleIds: [1]
    });
    expect(mockToastService.success).toHaveBeenCalled();
    expect(mockUserService.getUsers).toHaveBeenCalled(); // Should reload users
  });

  it('should handle user creation error', () => {
    component.userForm.patchValue({
      name: 'New User',
      email: 'newuser@example.com',
      roleIds: [1]
    });
    mockUserService.adminCreateUser.and.returnValue(throwError(() => new Error('Creation failed')));

    component.onSubmit();

    expect(mockToastService.error).toHaveBeenCalledWith('Creation failed');
  });

  it('should not submit if form is invalid', () => {
    component.userForm.patchValue({
      name: '',
      email: 'invalid-email',
      roleIds: []
    });

    component.onSubmit();

    expect(mockUserService.adminCreateUser).not.toHaveBeenCalled();
    expect(mockToastService.validationError).toHaveBeenCalled();
  });

  it('should toggle user status', () => {
    const user = mockUsers[0];
    mockDialogService.confirm.and.returnValue(Promise.resolve(true));
    mockUserService.toggleUserStatus.and.returnValue(of({ message: 'Status updated' }));

    component.toggleUserStatus(user);

    expect(mockDialogService.confirm).toHaveBeenCalled();
  });

  it('should reset user password', () => {
    const user = mockUsers[0];
    mockDialogService.confirm.and.returnValue(Promise.resolve(true));
    mockUserService.resetPassword.and.returnValue(of({ message: 'Password reset' }));

    component.resetPassword(user);

    expect(mockDialogService.confirm).toHaveBeenCalled();
  });

  it('should delete user', () => {
    const user = mockUsers[0];
    mockDialogService.confirm.and.returnValue(Promise.resolve(true));
    mockUserService.deleteUser.and.returnValue(of({ message: 'User deleted' }));

    component.deleteUser(user);

    expect(mockDialogService.confirm).toHaveBeenCalled();
  });

  it('should filter users based on search term', () => {
    component.allUsers = mockUsers;
    component.searchTerm = 'John';

    component.applyFilters();

    expect(component.filteredUsers.length).toBe(1);
    expect(component.filteredUsers[0].name).toBe('John Doe');
  });

  it('should reset form correctly', () => {
    component.userForm.patchValue({
      name: 'Test',
      email: 'test@example.com',
      roleIds: [1]
    });

    component.resetForm();

    expect(component.userForm.get('name')?.value).toBe('');
    expect(component.userForm.get('email')?.value).toBe('');
    expect(component.userForm.get('roleIds')?.value).toEqual([]);
  });

  it('should validate field invalidity correctly', () => {
    const nameControl = component.userForm.get('name');
    nameControl?.markAsTouched();
    nameControl?.setValue('');

    expect(component.isFieldInvalid('name')).toBeTruthy();
  });

  it('should validate minimum role requirement', () => {
    const control = component.userForm.get('roleIds');
    control?.setValue([]);

    const result = component.minimumRoleValidator(control!);
    expect(result).toEqual({ minimumRole: true });

    control?.setValue([1]);
    const validResult = component.minimumRoleValidator(control!);
    expect(validResult).toBeNull();
  });

  it('should display loading states correctly', () => {
    component.loading = true;
    fixture.detectChanges();

    const loadingElement = fixture.nativeElement.querySelector('.animate-spin');
    expect(loadingElement).toBeTruthy();
  });

  it('should display user table when not loading', () => {
    component.loading = false;
    component.filteredUsers = mockUsers;
    fixture.detectChanges();

    const table = fixture.nativeElement.querySelector('table');
    expect(table).toBeTruthy();
    
    const rows = fixture.nativeElement.querySelectorAll('tbody tr');
    expect(rows.length).toBe(mockUsers.length);
  });

  it('should display empty state when no users found', () => {
    component.loading = false;
    component.filteredUsers = [];
    fixture.detectChanges();

    const emptyState = fixture.nativeElement.querySelector('td[colspan="7"]');
    expect(emptyState).toBeTruthy();
    expect(emptyState.textContent).toContain('No users found');
  });
}); 