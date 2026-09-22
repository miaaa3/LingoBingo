import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { PlanningService } from 'src/app/Services/Planning/planning.service';
import { ClassroomService } from 'src/app/Services/Classroom/classroom.service';
import { LocalService } from 'src/app/Services/Auth/local.service';

@Component({
  selector: 'app-planning',
  templateUrl: './planning.component.html',
  styleUrls: ['./planning.component.css']
})
export class PlanningComponent implements OnInit {
  isTeacher = false;
  loading = false;
  events: any[] = [];
  classes: any[] = [];
  form!: FormGroup;
  typeLabels: { [k: string]: string } = { QUIZ_DUE: 'Quiz due', LIVE_SESSION: 'Live session', OTHER: 'Other' };

  constructor(
    private fb: FormBuilder,
    private planning: PlanningService,
    private classroom: ClassroomService,
    private local: LocalService,
    private toastr: ToastrService,
    private router: Router
  ) {}

  /** An event whose deadline has passed can no longer be acted on. */
  isPast(ev: any): boolean {
    const d = this.toDate(ev?.dueDate);
    return !!d && d.getTime() < Date.now();
  }

  /** For students, quiz/live-session entries are actionable until they pass. */
  isActionable(ev: any): boolean {
    return !this.isTeacher && !this.isPast(ev) &&
      (ev?.type === 'QUIZ_DUE' || ev?.type === 'LIVE_SESSION');
  }

  goToEvent(ev: any): void {
    if (!this.isActionable(ev)) { return; }
    this.router.navigate(['/Quizzes']);
  }

  ngOnInit(): void {
    const role = this.local.getData('role');
    this.isTeacher = role === 'TEACHER' || role === 'ADMIN';
    this.form = this.fb.group({
      classId: ['', Validators.required],
      title: ['', Validators.required],
      description: [''],
      type: ['QUIZ_DUE', Validators.required],
      dueDate: ['', Validators.required]
    });
    if (this.isTeacher) {
      this.classroom.myClasses().subscribe(c => this.classes = c || [], () => {});
    }
    this.load();
  }

  load(): void {
    this.loading = true;
    this.planning.mine().subscribe(
      e => { this.events = e || []; this.loading = false; },
      () => { this.loading = false; }
    );
  }

  add(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.planning.create(this.form.value).subscribe(
      () => {
        this.form.patchValue({ title: '', description: '', dueDate: '' });
        this.toastr.success('Added to the calendar', 'Success');
        this.load();   // refetch so the new event shows with its class name + formatted date
      },
      () => this.toastr.error('Could not add event', 'Error')
    );
  }

  remove(ev: any): void {
    this.planning.delete(ev.id).subscribe(
      () => { this.events = this.events.filter(x => x.id !== ev.id); this.toastr.success('Removed', ''); },
      () => this.toastr.error('Could not remove', 'Error')
    );
  }

  typeClass(t: string): string {
    if (t === 'QUIZ_DUE') { return 'bg-mustard-100 text-mustard-800'; }
    if (t === 'LIVE_SESSION') { return 'bg-mauve-100 text-mauve-800'; }
    return 'bg-gray-100 text-gray-600';
  }

  /**
   * Coerce a backend date into a JS Date the `date` pipe can format. Handles
   * ISO strings and Jackson's numeric-array form [year, month(1-based), day,
   * hour, minute, second]. Returns null for anything unparseable so the pipe
   * renders nothing instead of throwing (NG02100).
   */
  toDate(v: any): Date | null {
    if (v === null || v === undefined || v === '') { return null; }
    if (Array.isArray(v)) {
      const [y, mo = 1, d = 1, h = 0, mi = 0, s = 0] = v;
      const dt = new Date(y, (mo || 1) - 1, d || 1, h, mi, s);
      return isNaN(dt.getTime()) ? null : dt;
    }
    const dt = new Date(v);
    return isNaN(dt.getTime()) ? null : dt;
  }
}
