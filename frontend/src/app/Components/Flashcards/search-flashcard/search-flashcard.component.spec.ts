import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SearchFlashcardComponent } from './search-flashcard.component';

describe('SearchFlashcardComponent', () => {
  let component: SearchFlashcardComponent;
  let fixture: ComponentFixture<SearchFlashcardComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [SearchFlashcardComponent]
    });
    fixture = TestBed.createComponent(SearchFlashcardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
