import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ContactService } from '../../../services/contact.service';
import { ToastService } from '../../../services/toast.service';
import { ContactMessage } from '../../../models/user.model';

@Component({
  selector: 'app-contact-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './contact-form.component.html'
})
export class ContactFormComponent implements OnInit {
  contactForm: FormGroup;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private contactService: ContactService,
    private toastService: ToastService
  ) {
    this.contactForm = this.fb.group({
      name: ['', [
        Validators.required, 
        Validators.minLength(2), 
        Validators.maxLength(100)
      ]],
      email: ['', [
        Validators.required, 
        Validators.email
      ]],
      phoneNumber: ['', [
        Validators.required,
        Validators.pattern(/^[6-9]\d{9}$/)
      ]],
      message: ['', [
        Validators.required,
        Validators.minLength(10),
        Validators.maxLength(1000)
      ]]
    });
  }

  ngOnInit(): void {
    // Component initialization
  }

  onSubmit(): void {
    if (this.contactForm.valid && !this.isLoading) {
      this.isLoading = true;

      const contactData: ContactMessage = {
        name: this.contactForm.value.name,
        email: this.contactForm.value.email,
        phoneNumber: this.contactForm.value.phoneNumber,
        message: this.contactForm.value.message
      };

      this.contactService.submitContactForm(contactData).subscribe({
        next: (response) => {
          this.isLoading = false;
          this.toastService.contactMessageSent();
          this.contactForm.reset();
        },
        error: (error) => {
          this.isLoading = false;
          this.toastService.error(
            error.message || 'Failed to send message. Please try again.',
            'Send Failed'
          );
        }
      });
    } else {
      // Mark all fields as touched to show validation errors
      Object.keys(this.contactForm.controls).forEach(key => {
        this.contactForm.get(key)?.markAsTouched();
      });
      this.toastService.validationError('Please fill all required fields correctly.');
    }
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.contactForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }
} 

