import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ForecastHour, ScheduledSession, SessionSuggestion } from '../models';

@Injectable({ providedIn: 'root' })
export class ForecastService {
  private readonly base = '/api/sessions';

  forecast$ = new BehaviorSubject<ForecastHour[]>([]);
  sessions$ = new BehaviorSubject<ScheduledSession[]>([]);
  suggestions$ = new BehaviorSubject<SessionSuggestion[]>([]);

  constructor(private http: HttpClient) {}

  loadForecast() {
    return this.http.get<ForecastHour[]>(`${this.base}/forecast`)
      .pipe(tap(h => this.forecast$.next(h)));
  }

  loadSessions() {
    return this.http.get<ScheduledSession[]>(`${this.base}`)
      .pipe(tap(s => this.sessions$.next(s)));
  }

  loadSuggestions(durationHours: number) {
    return this.http.get<SessionSuggestion[]>(`${this.base}/suggestions`, { params: { durationHours } })
      .pipe(tap(s => this.suggestions$.next(s)));
  }

  scheduleSession(startTime: string, endTime: string) {
    return this.http.post(`${this.base}`, { startTime, endTime });
  }

  cancelSession(startTime: string) {
    return this.http.delete(`${this.base}`, { params: { startTime } });
  }
}
