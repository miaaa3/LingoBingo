import { Component, OnInit } from '@angular/core';
import { RecentActivity } from 'src/app/Models/recent-activity.model';
import { RecentActivityService } from 'src/app/Services/recent-activity.service';

@Component({
  selector: 'app-home-page',
  templateUrl: './home-page.component.html',
  styleUrls: ['./home-page.component.css']
})
export class HomePageComponent implements OnInit {
  recentActivities: RecentActivity[];

  constructor(private recentActivityService: RecentActivityService) {}

  ngOnInit(): void {
    this.loadRecentActivities();
  }

  loadRecentActivities(): void {
    this.recentActivityService.getRecentActivitiesForUser().subscribe(
      (recentActivitiesList: RecentActivity[]) => {
        console.warn(recentActivitiesList)
        this.recentActivities = recentActivitiesList;
      },
      (error) => {
        console.error('Error loading recent Activities:', error);
      }
    );
  }

}
