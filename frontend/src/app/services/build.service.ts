import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { BuildRequest, BuildResult } from '../models';

@Injectable({ providedIn: 'root' })
export class BuildService {
  private readonly base = '/api/build';

  result$ = new BehaviorSubject<BuildResult | null>(null);

  constructor(private http: HttpClient) {}

  evaluate(req: BuildRequest) {
    return this.http.post<BuildResult>(`${this.base}/evaluate`, req)
      .pipe(tap(r => this.result$.next(r)));
  }

  suggest(req: BuildRequest) {
    return this.http.post<BuildResult>(`${this.base}/suggest`, req)
      .pipe(tap(r => this.result$.next(r)));
  }
}
