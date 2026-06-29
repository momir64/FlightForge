import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, forkJoin } from 'rxjs';
import { tap } from 'rxjs/operators';
import { AirplaneSpecs, Battery, ESC, MotorConfiguration, Servo } from '../models';

@Injectable({ providedIn: 'root' })
export class ComponentService {
  private readonly base = '/api/components';

  airplanes$ = new BehaviorSubject<AirplaneSpecs[]>([]);
  motorConfigs$ = new BehaviorSubject<MotorConfiguration[]>([]);
  escs$ = new BehaviorSubject<ESC[]>([]);
  batteries$ = new BehaviorSubject<Battery[]>([]);
  servos$ = new BehaviorSubject<Servo[]>([]);

  constructor(private http: HttpClient) {}

  loadAll() {
    return forkJoin({
      airplanes: this.http.get<AirplaneSpecs[]>(`${this.base}/airplanes`),
      motorConfigs: this.http.get<MotorConfiguration[]>(`${this.base}/motor-configurations`),
      escs: this.http.get<ESC[]>(`${this.base}/escs`),
      batteries: this.http.get<Battery[]>(`${this.base}/batteries`),
      servos: this.http.get<Servo[]>(`${this.base}/servos`),
    }).pipe(tap(data => {
      this.airplanes$.next(data.airplanes);
      this.motorConfigs$.next(data.motorConfigs);
      this.escs$.next(data.escs);
      this.batteries$.next(data.batteries);
      this.servos$.next(data.servos);
    }));
  }
}
