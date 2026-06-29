import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { BuildRequest, Priority } from '../models';
import { SettingsService } from './settings.service';

export interface TopbarState {
  airplaneId: number | null;
  foamboardWeight: number;
  scaleFactor: number;
  minTWRatio: number | null;
  minFlightTime: number | null;
  priority: Priority;
  metalGearsPreference: boolean;
}

export interface ComponentState {
  selectedMotorId: number | null;
  motorConfigurationId: number | null;
  escId: number | null;
  batteryId: number | null;
  servoId: number | null;
}

const TOPBAR_KEY = 'flightforge.topbar';
const COMPONENTS_KEY = 'flightforge.components';

const DEFAULT_TOPBAR: TopbarState = {
  airplaneId: null,
  foamboardWeight: 2.927,
  scaleFactor: 1.0,
  minTWRatio: null,
  minFlightTime: null,
  priority: 'MIN_WEIGHT',
  metalGearsPreference: false,
};

const DEFAULT_COMPONENTS: ComponentState = {
  selectedMotorId: null,
  motorConfigurationId: null,
  escId: null,
  batteryId: null,
  servoId: null,
};

function loadFromStorage<T>(key: string, fallback: T): T {
  try {
    const raw = localStorage.getItem(key);
    if (!raw) return fallback;
    return { ...fallback, ...JSON.parse(raw) };
  } catch {
    return fallback;
  }
}

@Injectable({ providedIn: 'root' })
export class StateService {
  topbar$ = new BehaviorSubject<TopbarState>(loadFromStorage(TOPBAR_KEY, DEFAULT_TOPBAR));
  components$ = new BehaviorSubject<ComponentState>(loadFromStorage(COMPONENTS_KEY, DEFAULT_COMPONENTS));

  constructor(private settingsService: SettingsService) {
    this.topbar$.subscribe(t => localStorage.setItem(TOPBAR_KEY, JSON.stringify(t)));
    this.components$.subscribe(c => localStorage.setItem(COMPONENTS_KEY, JSON.stringify(c)));
  }

  buildRequest(): BuildRequest {
    const t = this.topbar$.value;
    const c = this.components$.value;
    const s = this.settingsService.settings$.value;
    return {
      airplaneId: t.airplaneId,
      foamboardWeight: t.foamboardWeight,
      scaleFactor: t.scaleFactor,
      minTWRatio: t.minTWRatio,
      minFlightTime: t.minFlightTime,
      priority: t.priority,
      metalGearsPreference: t.metalGearsPreference,
      location: s.location,
      sessionDuration: s.sessionDuration,
      motorConfigurationId: c.motorConfigurationId,
      escId: c.escId,
      batteryId: c.batteryId,
      servoId: c.servoId,
      receiverWeight: s.receiverWeight,
      receiverPowerConsumption: s.receiverPowerConsumption,
    };
  }

  patchTopbar(patch: Partial<TopbarState>) {
    this.topbar$.next({ ...this.topbar$.value, ...patch });
  }

  patchComponents(patch: Partial<ComponentState>) {
    this.components$.next({ ...this.components$.value, ...patch });
  }
}
