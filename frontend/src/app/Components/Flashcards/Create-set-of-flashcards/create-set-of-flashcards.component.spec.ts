import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormArray, FormGroup, FormBuilder, Validators } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms'; // Import ReactiveFormsModule
import { CreateSetOfFlashcardsComponent } from './create-set-of-flashcards.component'; // Your component

describe('CreateSetOfFlashcardsComponent', () => {
  let component: CreateSetOfFlashcardsComponent;
  let fixture: ComponentFixture<CreateSetOfFlashcardsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CreateSetOfFlashcardsComponent],
      imports: [ReactiveFormsModule], // Include ReactiveFormsModule
    }).compileComponents();

    fixture = TestBed.createComponent(CreateSetOfFlashcardsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize flashcard form with one card', () => {
    const flashcardsForm = component.flashcardSetForm.get('flashcards') as FormArray;
    expect(flashcardsForm.length).toBe(1);  // Initially, there should be one card
  });

  it('should add a new card when addNewCard is called', () => {
    component.addNewCard();
    const flashcardsForm = component.flashcardSetForm.get('flashcards') as FormArray;
    expect(flashcardsForm.length).toBe(2);  // After adding, there should be two cards
  });

  it('should handle file selection and update the form control', () => {
    const fileInput = fixture.nativeElement.querySelector('input[type="file"]');
    const file = new File(['dummy content'], 'example.png', { type: 'image/png' });

    const event = { target: { files: [file] } };
    component.onFileChange(event, 0);  // Simulate file selection for the first card

    fixture.detectChanges();
    const flashcardsForm = component.flashcardSetForm.get('flashcards') as FormArray;
    const selectedFile = flashcardsForm.at(0).get('image')?.value;

    expect(selectedFile).toBe(file);  // Check if the file is stored in the form control
  });

  it('should call createFlashcardSet and navigate on success', () => {
    spyOn(component, 'createFlashcardSet');  // Spy on createFlashcardSet method
    const button = fixture.nativeElement.querySelector('button');
    button.click();

    expect(component.createFlashcardSet).toHaveBeenCalled();  // Check if the method is called
  });
});
