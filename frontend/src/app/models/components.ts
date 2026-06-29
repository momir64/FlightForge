export type GearType = 'PLASTIC' | 'METAL';

export interface Motor {
  id: number;
  name: string;
  weight: number;
  price: number;
  available: boolean;
}

export interface Propeller {
  id: number;
  diameter: number;
  pitch: number;
  bladeCount: number;
  weight: number;
  price: number;
  available: boolean;
}

export interface MotorConfiguration {
  id: number;
  motor: Motor;
  propeller: Propeller;
  cellCount: number;
  thrust: number;
  maxCurrent: number;
}

export interface ESC {
  id: number;
  name: string;
  continuousCurrent: number;
  burstCurrent: number;
  minCellCount: number;
  maxCellCount: number;
  becOutputVoltage: number;
  becMaxCurrent: number;
  weight: number;
  price: number;
  available: boolean;
}

export interface Battery {
  id: number;
  name: string;
  cellCount: number;
  capacity: number;
  cRating: number;
  weight: number;
  price: number;
  available: boolean;
}

export interface Servo {
  id: number;
  name: string;
  torque: number;
  gearType: GearType;
  sizeCategory: number;
  stallCurrent: number;
  weight: number;
  price: number;
  available: boolean;
}

export interface Receiver {
  id: number;
  weight: number;
  powerConsumption: number;
}
