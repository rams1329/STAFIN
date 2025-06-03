import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { ConfirmationDialogComponent, ConfirmationDialogData } from './confirmation-dialog.component';

describe('ConfirmationDialogComponent', () => {
  let component: ConfirmationDialogComponent;
  let fixture: ComponentFixture<ConfirmationDialogComponent>;
  let mockDialogData: ConfirmationDialogData;

  beforeEach(async () => {
    mockDialogData = {
      title: 'Test Title',
      message: 'Test message',
      confirmText: 'Confirm',
      cancelText: 'Cancel',
      type: 'warning'
    };

    await TestBed.configureTestingModule({
      imports: [ConfirmationDialogComponent, FormsModule],
      providers: [
        { provide: 'dialogData', useValue: mockDialogData }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ConfirmationDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display the title and message', () => {
    const compiled = fixture.nativeElement;
    expect(compiled.querySelector('h3').textContent).toContain('Test Title');
    expect(compiled.querySelector('p').textContent).toContain('Test message');
  });

  it('should display confirm and cancel buttons with correct text', () => {
    const compiled = fixture.nativeElement;
    const buttons = compiled.querySelectorAll('button');
    expect(buttons[0].textContent.trim()).toBe('Cancel');
    expect(buttons[1].textContent.trim()).toBe('Confirm');
  });

  it('should set default type to warning if not provided', () => {
    const dataWithoutType = {
      title: 'Test',
      message: 'Test message'
    };
    
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      imports: [ConfirmationDialogComponent, FormsModule],
      providers: [
        { provide: 'dialogData', useValue: dataWithoutType }
      ]
    });

    const newFixture = TestBed.createComponent(ConfirmationDialogComponent);
    const newComponent = newFixture.componentInstance;
    expect(newComponent.data.type).toBe('warning');
  });

  it('should show double confirmation input when required', () => {
    const dataWithDoubleConfirm: ConfirmationDialogData = {
      title: 'Delete Item',
      message: 'Are you sure?',
      requireDoubleConfirm: true,
      itemName: 'test-item'
    };

    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      imports: [ConfirmationDialogComponent, FormsModule],
      providers: [
        { provide: 'dialogData', useValue: dataWithDoubleConfirm }
      ]
    });

    const newFixture = TestBed.createComponent(ConfirmationDialogComponent);
    newFixture.detectChanges();
    
    const compiled = newFixture.nativeElement;
    const input = compiled.querySelector('input[type="text"]');
    expect(input).toBeTruthy();
    expect(compiled.querySelector('label').textContent).toContain('test-item');
  });

  it('should validate double confirmation text correctly', () => {
    component.data.requireDoubleConfirm = true;
    component.data.itemName = 'test-item';
    
    component.confirmationText = 'wrong-text';
    component.onConfirmationTextChange();
    expect(component.isDoubleConfirmValid).toBeFalsy();

    component.confirmationText = 'test-item';
    component.onConfirmationTextChange();
    expect(component.isDoubleConfirmValid).toBeTruthy();
  });

  it('should disable confirm button when double confirmation is invalid', () => {
    component.data.requireDoubleConfirm = true;
    component.data.itemName = 'test-item';
    component.confirmationText = 'wrong';
    component.isDoubleConfirmValid = false;
    
    fixture.detectChanges();
    
    const confirmButton = fixture.nativeElement.querySelectorAll('button')[1];
    expect(confirmButton.disabled).toBeTruthy();
  });

  it('should call window.confirmDialogResult on confirm', () => {
    const mockWindow = {
      confirmDialogResult: jasmine.createSpy('confirmDialogResult')
    };
    spyOnProperty(window, 'window', 'get').and.returnValue(mockWindow as any);

    component.onConfirm();
    expect(mockWindow.confirmDialogResult).toHaveBeenCalledWith(true);
  });

  it('should call window.confirmDialogResult on cancel', () => {
    const mockWindow = {
      confirmDialogResult: jasmine.createSpy('confirmDialogResult')
    };
    spyOnProperty(window, 'window', 'get').and.returnValue(mockWindow as any);

    component.onCancel();
    expect(mockWindow.confirmDialogResult).toHaveBeenCalledWith(false);
  });

  it('should display correct icon based on type', () => {
    const compiled = fixture.nativeElement;
    
    // Test warning icon (default)
    expect(compiled.querySelector('svg')).toBeTruthy();
    
    // Test danger type
    component.data.type = 'danger';
    fixture.detectChanges();
    expect(compiled.querySelector('.bg-red-100')).toBeTruthy();
    
    // Test info type
    component.data.type = 'info';
    fixture.detectChanges();
    expect(compiled.querySelector('.bg-blue-100')).toBeTruthy();
  });

  it('should prevent confirm when double confirmation is required but invalid', () => {
    const mockWindow = {
      confirmDialogResult: jasmine.createSpy('confirmDialogResult')
    };
    spyOnProperty(window, 'window', 'get').and.returnValue(mockWindow as any);

    component.data.requireDoubleConfirm = true;
    component.isDoubleConfirmValid = false;

    component.onConfirm();
    expect(mockWindow.confirmDialogResult).not.toHaveBeenCalled();
  });
}); 