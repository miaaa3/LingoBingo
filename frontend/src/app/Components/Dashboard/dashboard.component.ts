import { Component, OnInit } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DashboardService } from 'src/app/Services/Dashboard/dashboard.service';
import { LocalService } from 'src/app/Services/Auth/local.service';
import { CurrentUserService } from 'src/app/Services/Auth/current-user.service';
import { FlashcardSetService } from 'src/app/Services/Flashcards/flashcardSet.service';
import { QuizService } from 'src/app/Services/Quiz/quiz.service';
import { ClassroomService } from 'src/app/Services/Classroom/classroom.service';
import { PlanningService } from 'src/app/Services/Planning/planning.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  isTeacher = false;
  loading = true;

  // Student dashboard payload (gamified)
  d: any = null;

  // Teacher dashboard (built from what they've created)
  teacherName = '';
  setCount = 0;
  quizCount = 0;
  classCount = 0;
  upcoming: any[] = [];

  constructor(
    private dash: DashboardService,
    private local: LocalService,
    private currentUser: CurrentUserService,
    private setsApi: FlashcardSetService,
    private quizApi: QuizService,
    private classApi: ClassroomService,
    private planningApi: PlanningService
  ) {}

  ngOnInit(): void {
    const role = this.local.getData('role');
    this.isTeacher = role === 'TEACHER' || role === 'ADMIN';
    if (this.isTeacher) {
      this.loadTeacher();
    } else {
      this.loadStudent();
    }
  }

  private loadStudent(): void {
    this.dash.me().subscribe(
      r => { this.d = r; this.loading = false; },
      () => { this.loading = false; }
    );
  }

  private loadTeacher(): void {
    this.teacherName = this.currentUser.name || 'there';
    forkJoin({
      sets: this.setsApi.getVisibleFlashcardSets().pipe(catchError(() => of({ ownSets: [] } as any))),
      quizzes: this.quizApi.getVisibleQuizzes().pipe(catchError(() => of({ ownQuizzes: [] } as any))),
      classes: this.classApi.myClasses().pipe(catchError(() => of([] as any[]))),
      planning: this.planningApi.mine().pipe(catchError(() => of([] as any[])))
    }).subscribe({
      next: res => {
        this.setCount = (res.sets?.ownSets || []).length;
        this.quizCount = (res.quizzes?.ownQuizzes || []).length;
        this.classCount = (res.classes || []).length;
        this.upcoming = this.sortUpcoming(res.planning || []).slice(0, 5);
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  /** Sort planning events by whichever date field they carry, soonest first. */
  private sortUpcoming(events: any[]): any[] {
    const when = (e: any) => new Date(e?.date || e?.dueDate || e?.startDate || 0).getTime();
    return [...events].sort((a, b) => when(a) - when(b));
  }

  eventDate(e: any): Date | null {
    const v = e?.date || e?.dueDate || e?.startDate;
    if (v === null || v === undefined || v === '') { return null; }
    if (Array.isArray(v)) {
      const [y, mo = 1, d = 1, h = 0, mi = 0, s = 0] = v;
      const dt = new Date(y, (mo || 1) - 1, d || 1, h, mi, s);
      return isNaN(dt.getTime()) ? null : dt;
    }
    const dt = new Date(v);
    return isNaN(dt.getTime()) ? null : dt;
  }

  // --- student helpers (unchanged) ---
  xpPct(): number {
    return this.d && this.d.xpForLevel ? Math.round((this.d.xpIntoLevel / this.d.xpForLevel) * 100) : 0;
  }

  initial(): string {
    if (this.isTeacher) {
      return this.teacherName ? this.teacherName.charAt(0).toUpperCase() : '?';
    }
    return this.d && this.d.name ? this.d.name.charAt(0).toUpperCase() : '?';
  }

  typeLabel(t: string): string {
    return t === 'QUIZ_DUE' ? 'QUIZ' : t === 'LIVE_SESSION' ? 'LIVE' : 'EVENT';
  }
}
