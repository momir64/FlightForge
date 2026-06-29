import { AirplaneSpecs } from './airplane';
import { MotorConfiguration, ESC, Battery, Servo, Receiver } from './components';

export type Priority = 'MIN_PRICE' | 'MIN_WEIGHT' | 'MAX_FLIGHT_TIME' | 'MAX_TW_FACTOR';
export type Maneuverability = 'EASY' | 'MEDIUM' | 'HARD';
export type BuildWarningType = 'INCOMPATIBLE_BATTERY' | 'INCOMPATIBLE_ESC' | 'INCOMPATIBLE_BEC' | 'INSUFFICIENT_THRUST' | 'INSUFFICIENT_CAPACITY';

export interface UserPreferences {
  foamboardWeight: number;
  scaleFactor: number;
  minTWRatio: number | null;
  minFlightTime: number | null;
  priority: Priority;
  metalGearsPreference: boolean;
  location: string;
}

export interface BuildConfig {
  airplane: AirplaneSpecs;
  userPreferences: UserPreferences;
  motorConfiguration: MotorConfiguration | null;
  esc: ESC | null;
  battery: Battery | null;
  servos: Servo[] | null;
  receiver: Receiver | null;
  correctedDryWeight: number | null;
  allUpWeight: number | null;
  totalMaxConsumption: number | null;
  twFactor: number | null;
  wingLoading: number | null;
  wclFactor: number | null;
  estimatedFlightTime: number | null;
  totalPrice: number | null;
  maneuverability: Maneuverability | null;
  windSpeedLowerThreshold: number | null;
  windSpeedUpperThreshold: number | null;
}

export interface BuildWarning {
  type: BuildWarningType;
  message: string;
  relatedComponent: string;
}

export interface BuildResult {
  build: BuildConfig;
  warnings: BuildWarning[];
}

export interface BuildRequest {
  airplaneId: number | null;
  foamboardWeight: number;
  scaleFactor: number;
  minTWRatio: number | null;
  minFlightTime: number | null;
  priority: Priority;
  metalGearsPreference: boolean;
  location: string;
  motorConfigurationId: number | null;
  escId: number | null;
  batteryId: number | null;
  servoId: number | null;
  receiverWeight: number;
  receiverPowerConsumption: number;
}
