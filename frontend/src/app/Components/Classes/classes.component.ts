import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { ClassroomService } from 'src/app/Services/Classroom/classroom.service';
import { LocalService } from 'src/app/Services/Auth/local.service';

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

  constructor(
    private fb: FormBuilder,
    private classroomService: ClassroomService,
    private toastr: ToastrService,
    private local: LocalService
  ) {}

  ngOnInit(): void {
    const role = this.local.getData('role');
    this.isTeacher = role === 'TEACHER' || role === 'ADMIN';
    this.createForm = this.fb.group({ name: ['', Validators.required], description: [''] });
    this.joinForm = this.fb.group({ joinCode: ['', Validators.required] });
    this.loadClasses();
  }

  loadClasses(): void {
    this.loading = true;
    this.classroomService.myClasses().subscribe(
      res => { this.classes = res || []; this.loading = false; },
      () => { this.loading = false; }
    );
  }

  create(): void {
    if (this.createForm.invalid) { return; }
    this.classroomService.createClass(this.createForm.value).subscribe(
      res => {
        this.classes = [res, ...this.classes];
        this.createForm.reset();
        this.toastr.success('Class created', 'Success');
      },
      () => this.toastr.error('Could not create class', 'Error')
    );
  }

  join(): void {
    if (this.joinForm.invalid) { return; }
    this.classroomService.joinClass(this.joinForm.get('joinCode')?.value).subscribe(
      res => {
        this.classes = [res, ...this.classes];
        this.joinForm.reset();
        this.toastr.success('Joined class', 'Success');
      },
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
}
