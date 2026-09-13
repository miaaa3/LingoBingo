import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { LessonService } from 'src/app/Services/Lesson/lesson.service';

@Component({
  selector: 'app-create-course',
  templateUrl: './create-course.component.html',
  styleUrls: ['./create-course.component.css']
})
export class CreateCourseComponent implements OnInit {
  form!: FormGroup;
  files: (File | null)[] = [];
  saving = false;

  constructor(
    private fb: FormBuilder,
    private lessonService: LessonService,
    private router: Router,
    private toastr: ToastrService
  ) {}

  get sections(): FormArray { return this.form.get('sections') as FormArray; }

  ngOnInit(): void {
    this.form = this.fb.group({
      title: ['', Validators.required],
      isPublic: [true],
      sections: this.fb.array([])
    });
    this.addSection();
  }

  addSection(): void {
    this.sections.push(this.fb.group({
      heading: ['', Validators.required],
      content: ['', Validators.required]
    }));
    this.files.push(null);
  }

  removeSection(i: number): void {
    if (this.sections.length <= 1) { this.toastr.warning('A course needs at least one section', ''); return; }
    this.sections.removeAt(i);
    this.files.splice(i, 1);
  }

  onFile(i: number, ev: any): void {
    const f = ev.target.files && ev.target.files[0];
    this.files[i] = f || null;
  }

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    const fd = new FormData();
    fd.append('title', this.form.get('title')?.value);
    fd.append('isPublic', String(this.form.get('isPublic')?.value));
    this.sections.controls.forEach((sec, i) => {
      fd.append(`sections[${i}].heading`, sec.get('heading')?.value);
      fd.append(`sections[${i}].content`, sec.get('content')?.value);
      const file = this.files[i];
      if (file) { fd.append(`sections[${i}].media`, file); }
    });
    this.lessonService.createCourse(fd).subscribe(
      () => { this.toastr.success('Course created', 'Success'); this.router.navigate(['/Courses']); },
      () => { this.saving = false; this.toastr.error('Could not create course', 'Error'); }
    );
  }
}
