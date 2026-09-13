import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DisplaySetOfFlashcardsComponent } from './display-set-of-flashcards.component';

describe('DisplaySetOfFlashcardsComponent', () => {
  let component: DisplaySetOfFlashcardsComponent;
  let fixture: ComponentFixture<DisplaySetOfFlashcardsComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DisplaySetOfFlashcardsComponent]
    });
    fixture = TestBed.createComponent(DisplaySetOfFlashcardsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
