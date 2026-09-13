import { Component, OnInit } from '@angular/core';
import { DashboardService } from 'src/app/Services/Dashboard/dashboard.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  d: any = null;
  loading = true;

  constructor(private dash: DashboardService) {}

  ngOnInit(): void {
    this.dash.me().subscribe(
      r => { this.d = r; this.loading = false; },
      () => { this.loading = false; }
    );
  }

  xpPct(): number {
    return this.d && this.d.xpForLevel ? Math.round((this.d.xpIntoLevel / this.d.xpForLevel) * 100) : 0;
  }

  initial(): string {
    return this.d && this.d.name ? this.d.name.charAt(0).toUpperCase() : '?';
  }

  typeLabel(t: string): string {
    return t === 'QUIZ_DUE' ? 'QUIZ' : t === 'LIVE_SESSION' ? 'LIVE' : 'EVENT';
  }
}
