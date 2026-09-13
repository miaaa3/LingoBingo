import { Component, OnInit } from '@angular/core';
import { LessonService } from 'src/app/Services/Lesson/lesson.service';
import { LocalService } from 'src/app/Services/Auth/local.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-courses',
  templateUrl: './courses.component.html',
  styleUrls: ['./courses.component.css']
})
export class CoursesComponent implements OnInit {
  isTeacher = false;
  courses: any[] = [];
  loading = false;

  constructor(
    private lessonService: LessonService,
    private local: LocalService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    const role = this.local.getData('role');
    this.isTeacher = role === 'TEACHER' || role === 'ADMIN';
    this.load();
  }

  load(): void {
    this.loading = true;
    const done = (res: any[]) => { this.courses = res || []; this.loading = false; };
    const fail = () => { this.loading = false; };
    if (this.isTeacher) {
      this.lessonService.getMyCourses(this.local.getData('userid')).subscribe(done, fail);
    } else {
      this.lessonService.getPublicCourses().subscribe(done, fail);
    }
  }

  sectionCount(c: any): number {
    return c.sections ? c.sections.length : 0;
  }

  toggleVisibility(c: any, ev: Event): void {
    ev.stopPropagation();
    this.lessonService.toggleVisibility(c.id).subscribe(
      (res: any) => { c.isPublic = res.isPublic; this.toastr.success('Visibility updated', ''); },
      () => this.toastr.error('Could not update visibility', 'Error')
    );
  }

  remove(c: any, ev: Event): void {
    ev.stopPropagation();
    this.lessonService.deleteCourse(c.id).subscribe(
      () => { this.courses = this.courses.filter(x => x.id !== c.id); this.toastr.success('Course deleted', ''); },
      () => this.toastr.error('Could not delete course', 'Error')
    );
  }
}
