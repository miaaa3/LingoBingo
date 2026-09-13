import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { ClassroomService } from 'src/app/Services/Classroom/classroom.service';
import { LocalService } from 'src/app/Services/Auth/local.service';
import { ClassContentService } from 'src/app/Services/Classroom/class-content.service';
import { QuizService } from 'src/app/Services/Quiz/quiz.service';
import { LessonService } from 'src/app/Services/Lesson/lesson.service';
import { FlashcardSetService } from 'src/app/Services/Flashcards/flashcardSet.service';

@Component({
  selector: 'app-classes',
  templateUrl: './classes.component.html',
  styleUrls: ['./classes.component.css']
})
export class ClassesComponent implements OnInit {
  isTeacher = false;
  classes: any[] = [];
  loading = false;

  createForm!: FormGroup;
  joinForm!: FormGroup;

  rosterFor: number | null = null;
  roster: any[] = [];

  // content assignment
  expandedClassId: number | null = null;
  contentByClass: { [id: number]: any[] } = {};
  assignType = 'COURSE';
  assignItemId: any = '';
  myQuizzes: any[] = [];
  myCourses: any[] = [];
  mySets: any[] = [];

  constructor(
    private fb: FormBuilder,
    private classroomService: ClassroomService,
    private toastr: ToastrService,
    private local: LocalService,
    private classContent: ClassContentService,
    private quizService: QuizService,
    private lessonService: LessonService,
    private flashcardSetService: FlashcardSetService
  ) {}

  ngOnInit(): void {
    const role = this.local.getData('role');
    this.isTeacher = role === 'TEACHER' || role === 'ADMIN';
    this.createForm = this.fb.group({ name: ['', Validators.required], description: [''] });
    this.joinForm = this.fb.group({ joinCode: ['', Validators.required] });
    this.loadClasses();
    if (this.isTeacher) { this.loadMyContent(); }
  }

  loadClasses(): void {
    this.loading = true;
    this.classroomService.myClasses().subscribe(
      res => { this.classes = res || []; this.loading = false; },
      () => { this.loading = false; }
    );
  }

  loadMyContent(): void {
    this.quizService.getVisibleQuizzes().subscribe((r: any) => this.myQuizzes = r?.ownQuizzes || [], () => {});
    this.lessonService.getMyCourses(this.local.getData('userid')).subscribe(r => this.myCourses = r || [], () => {});
    this.flashcardSetService.getVisibleFlashcardSets().subscribe((r: any) => this.mySets = r?.ownSets || [], () => {});
  }

  create(): void {
    if (this.createForm.invalid) { return; }
    this.classroomService.createClass(this.createForm.value).subscribe(
      res => { this.classes = [res, ...this.classes]; this.createForm.reset(); this.toastr.success('Class created', 'Success'); },
      () => this.toastr.error('Could not create class', 'Error')
    );
  }

  join(): void {
    if (this.joinForm.invalid) { return; }
    this.classroomService.joinClass(this.joinForm.get('joinCode')?.value).subscribe(
      res => { this.classes = [res, ...this.classes]; this.joinForm.reset(); this.toastr.success('Joined class', 'Success'); },
      err => this.toastr.error(err?.error || 'Could not join class', 'Error')
    );
  }

  viewRoster(classId: number): void {
    if (this.rosterFor === classId) { this.rosterFor = null; return; }
    this.classroomService.members(classId).subscribe(
      res => { this.roster = res || []; this.rosterFor = classId; },
      () => this.toastr.error('Could not load roster', 'Error')
    );
  }

  copyCode(code: string): void {
    if (navigator.clipboard) { navigator.clipboard.writeText(code); }
    this.toastr.info('Join code copied', '');
  }

  toggleContent(c: any): void {
    if (this.expandedClassId === c.id) { this.expandedClassId = null; return; }
    this.expandedClassId = c.id;
    this.assignType = 'COURSE';
    this.assignItemId = '';
    this.classContent.forClass(c.id).subscribe(
      res => this.contentByClass[c.id] = res || [],
      () => this.contentByClass[c.id] = []
    );
  }

  itemsForType(): any[] {
    if (this.assignType === 'QUIZ') { return this.myQuizzes; }
    if (this.assignType === 'FLASHCARD_SET') { return this.mySets; }
    return this.myCourses;
  }

  itemLabel(item: any): string { return item.name || item.title || ('#' + item.id); }

  assign(): void {
    if (!this.expandedClassId || !this.assignItemId) { return; }
    const classId = this.expandedClassId;
    this.classContent.assign({ classId, contentType: this.assignType, contentId: +this.assignItemId }).subscribe(
      (res: any) => { this.contentByClass[classId] = res || []; this.assignItemId = ''; this.toastr.success('Assigned', ''); },
      err => this.toastr.error(err?.error || 'Could not assign', 'Error')
    );
  }

  unassign(classId: number, cc: any): void {
    this.classContent.unassign(cc.id).subscribe(
      () => { this.contentByClass[classId] = (this.contentByClass[classId] || []).filter(x => x.id !== cc.id); this.toastr.success('Removed', ''); },
      () => this.toastr.error('Could not remove', 'Error')
    );
  }

  contentLink(cc: any): any[] {
    if (cc.contentType === 'QUIZ') { return ['/Quizzes', cc.contentId, 'take']; }
    if (cc.contentType === 'COURSE') { return ['/Courses', cc.contentId]; }
    if (cc.contentType === 'FLASHCARD_SET') { return ['/Display-set-of-flashcard', cc.contentId]; }
    return ['/Home'];
  }

  typeLabel(t: string): string {
    return t === 'QUIZ' ? 'Quiz' : t === 'COURSE' ? 'Course' : t === 'FLASHCARD_SET' ? 'Flashcards' : t;
  }
}
