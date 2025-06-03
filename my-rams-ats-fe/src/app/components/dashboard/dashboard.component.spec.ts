import { ComponentFixture, TestBed } from '@angular/core/testing';
import {  } from './dashboard.component';

describe('', () => {
  let component: ;
  let fixture: ComponentFixture<>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: []
    }).compileComponents();

    fixture = TestBed.createComponent();
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
