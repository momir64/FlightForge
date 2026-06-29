import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Settings } from '../models';

const STORAGE_KEY = 'flightforge.settings';

const DEFAULT_SETTINGS: Settings = {
  location: '',
  receiverWeight: 8.5,
  receiverPowerConsumption: 45.0,
  sessionDuration: 60,
};

@Injectable({ providedIn: 'root' })
export class SettingsService {
  settings$ = new BehaviorSubject<Settings>(loadSettings());

  constructor() {
    this.settings$.subscribe(s => localStorage.setItem(STORAGE_KEY, JSON.stringify(s)));
  }

  update(settings: Settings) {
    this.settings$.next(settings);
  }
}

function loadSettings(): Settings {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return DEFAULT_SETTINGS;
    return { ...DEFAULT_SETTINGS, ...JSON.parse(raw) };
  } catch {
    return DEFAULT_SETTINGS;
  }
}
