import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AllQuizzesComponent } from './all-quizzes.component';

describe('AllQuizzesComponent', () => {
  let component: AllQuizzesComponent;
  let fixture: ComponentFixture<AllQuizzesComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AllQuizzesComponent]
    });
    fixture = TestBed.createComponent(AllQuizzesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
