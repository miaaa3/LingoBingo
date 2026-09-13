export class Flashcard {
    id?: number;
    term: string;
    definition: string;
    flipped: boolean; 

    constructor(term: string, definition: string) {
      this.term = term;
      this.definition = definition;
      this.flipped = false; 
    }
}
  