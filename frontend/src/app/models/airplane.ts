export type ControlSurfaceType = 'RUDDER_ELEVATOR' | 'RUDDER_ELEVATOR_AILERONS' | 'ELEVONS';

export interface AirplaneSpecs {
  id: number;
  name: string;
  length: number;
  wingspan: number;
  wingArea: number;
  dryWeight: number;
  allUpWeight: number;
  controlSurfaceType: ControlSurfaceType;
  recommendedTwFactor: number;
}
