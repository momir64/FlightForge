import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ForecastHour, ScheduledSession } from '../models';

@Injectable({ providedIn: 'root' })
export class ForecastService {
  private readonly base = '/api/sessions';

  forecast$ = new BehaviorSubject<ForecastHour[]>([]);
  sessions$ = new BehaviorSubject<ScheduledSession[]>([]);

  constructor(private http: HttpClient) {}

  loadForecast() {
    return this.http.get<ForecastHour[]>(`${this.base}/forecast`)
      .pipe(tap(h => this.forecast$.next(h)));
  }

  loadSessions() {
    return this.http.get<ScheduledSession[]>(`${this.base}`)
      .pipe(tap(s => this.sessions$.next(s)));
  }

  scheduleSession(startTime: string, endTime: string) {
    return this.http.post(`${this.base}`, { startTime, endTime });
  }

  cancelSession(startTime: string) {
    return this.http.delete(`${this.base}`, { params: { startTime } });
  }

  getClock() {
    return this.http.get<string>(`${this.base}/clock`);
  }

  setClock(time: string) {
    return this.http.post(`${this.base}/clock`, { time });
  }

  insertForecastHour(hour: ForecastHour) {
    return this.http.post(`${this.base}/forecast`, hour);
  }
}
