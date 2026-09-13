import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { LessonService } from 'src/app/Services/Lesson/lesson.service';

@Component({
  selector: 'app-course-viewer',
  templateUrl: './course-viewer.component.html',
  styleUrls: ['./course-viewer.component.css']
})
export class CourseViewerComponent implements OnInit {
  course: any = null;
  loading = true;

  constructor(private route: ActivatedRoute, private lessonService: LessonService) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.lessonService.getCourse(id).subscribe(
        res => { this.course = res; this.loading = false; },
        () => { this.loading = false; }
      );
    }
  }

  imageUrl(sectionId: number): string {
    return this.lessonService.sectionImageUrl(sectionId);
  }
}
