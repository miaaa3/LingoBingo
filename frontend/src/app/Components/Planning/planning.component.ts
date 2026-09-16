import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
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
    private toastr: ToastrService
  ) {}

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
}
