import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { ForecastService } from '../../services/forecast.service';
import { ForecastHour, ScheduledSession } from '../../models';

@Component({
  selector: 'app-forecast',
  standalone: true,
  imports: [CommonModule, MatButtonModule],
  templateUrl: './forecast.component.html',
  styleUrls: ['./forecast.component.scss'],
})
export class ForecastComponent implements OnInit {
  forecast: ForecastHour[] = [];
  sessions: ScheduledSession[] = [];

  dates: string[] = [];
  selectedDate: string | null = null;
  hoursForDate: ForecastHour[] = [];

  selectionStep: 0 | 1 = 0;
  selectionStart: string | null = null;

  constructor(
    private forecastService: ForecastService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit() {
    this.forecastService.forecast$.subscribe(f => {
      this.forecast = f;
      this.updateDates();
      this.updateHours();
      this.cdr.markForCheck();
    });
    this.forecastService.sessions$.subscribe(s => {
      this.sessions = s;
      this.cdr.markForCheck();
    });

    this.forecastService.loadForecast().subscribe();
    this.forecastService.loadSessions().subscribe();
  }

  private updateDates() {
    this.dates = [...new Set(this.forecast.map(h => h.timestamp.substring(0, 10)))];
    if (!this.selectedDate && this.dates.length) this.selectedDate = this.dates[0];
  }

  private updateHours() {
    if (!this.selectedDate) { this.hoursForDate = []; return; }
    this.hoursForDate = this.forecast.filter(h => h.timestamp.startsWith(this.selectedDate!));
  }

  selectDate(d: string) {
    this.selectedDate = d;
    this.updateHours();
  }

  dateQuality(d: string): 'ideal' | 'ok' | 'bad' {
    const hours = this.forecast.filter(h => h.timestamp.startsWith(d));
    const unsuitable = hours.filter(h => h.suitability === 'UNSUITABLE').length;
    const ideal = hours.filter(h => h.suitability === 'IDEAL').length;
    if (unsuitable > hours.length / 2) return 'bad';
    if (ideal > hours.length / 3) return 'ideal';
    return 'ok';
  }

  hourCellClass(h: ForecastHour): string {
    const suit = h.suitability ?? 'unclassified';
    const selecting = this.selectionStep === 1 && this.selectionStart === h.timestamp ? 'selecting-start' : '';
    return [suit, selecting].filter(Boolean).join(' ');
  }

  onHourClick(h: ForecastHour) {
    if (this.selectionStep === 0) {
      this.selectionStart = h.timestamp;
      this.selectionStep = 1;
    } else {
      if (h.timestamp <= this.selectionStart!) { this.resetSelection(); return; }
      this.forecastService.scheduleSession(this.selectionStart!, h.timestamp).subscribe({
        next: () => {
          this.forecastService.loadSessions().subscribe();
          this.resetSelection();
        },
        error: (e) => { alert('Failed to schedule: ' + e.error); this.resetSelection(); },
      });
    }
  }

  resetSelection() {
    this.selectionStep = 0;
    this.selectionStart = null;
  }

  cancelSession(startTime: string) {
    this.forecastService.cancelSession(startTime).subscribe({
      next: () => this.forecastService.loadSessions().subscribe(),
      error: (e) => alert('Failed to cancel: ' + e.error),
    });
  }

  formatTime(ts: string): string { return ts.substring(11, 16); }
  formatDate(ts: string): string {
    const [y, m, d] = ts.substring(0, 10).split('-');
    return `${d}.${m}.${y}.`;
  }
  formatDayName(d: string): string { return new Date(d).toLocaleDateString('en', { weekday: 'short' }); }
  formatDayNum(d: string): number { return new Date(d).getDate(); }
}
