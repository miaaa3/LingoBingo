import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DisplayFlashcardsComponent } from './display-flashcards.component';

describe('DisplayFlashcardsComponent', () => {
  let component: DisplayFlashcardsComponent;
  let fixture: ComponentFixture<DisplayFlashcardsComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DisplayFlashcardsComponent]
    });
    fixture = TestBed.createComponent(DisplayFlashcardsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
