import { Component, OnInit } from '@angular/core';
import { RecentActivity } from 'src/app/Models/recent-activity.model';
import { RecentActivityService } from 'src/app/Services/recent-activity.service';
import { CurrentUserService } from 'src/app/Services/Auth/current-user.service';
import { LocalService } from 'src/app/Services/Auth/local.service';

@Component({
  selector: 'app-home-page',
  templateUrl: './home-page.component.html',
  styleUrls: ['./home-page.component.css']
})
export class HomePageComponent implements OnInit {
  recentActivities: RecentActivity[];
  isTeacher = false;

  constructor(
    private recentActivityService: RecentActivityService,
    public currentUser: CurrentUserService,
    private local: LocalService
  ) {}

  ngOnInit(): void {
    const role = this.local.getData('role');
    this.isTeacher = role === 'TEACHER' || role === 'ADMIN';
    this.loadRecentActivities();
  }

  loadRecentActivities(): void {
    this.recentActivityService.getRecentActivitiesForUser().subscribe(
      (recentActivitiesList: RecentActivity[]) => {
        this.recentActivities = (recentActivitiesList || []).slice(0, 4);
      },
      (error) => {
        console.error('Error loading recent Activities:', error);
      }
    );
  }

  actionText(a: any): string {
    const v = (a?.action || '').toUpperCase();
    return v === 'CREATED' ? 'Created' : v === 'UPDATED' ? 'Updated' : v === 'DELETED' ? 'Deleted' : 'Activity';
  }

  typeText(a: any): string {
    return a?.type === 'QUIZ' ? 'quiz' : a?.type === 'FLASHCARD_SET' ? 'flashcard set' : 'item';
  }

  actionClass(a: any): string {
    const v = (a?.action || '').toUpperCase();
    if (v === 'CREATED') { return 'bg-green-100 text-green-700 dark:bg-[#0f2a1a] dark:text-green-400'; }
    if (v === 'DELETED') { return 'bg-red-100 text-red-600 dark:bg-[#2a1416] dark:text-red-400'; }
    return 'bg-blue-ribbon-50 text-blue-ribbon-700 dark:bg-[#232842] dark:text-blue-ribbon-300';
  }
}
