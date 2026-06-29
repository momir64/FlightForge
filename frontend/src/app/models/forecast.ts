export type HourSuitability = 'IDEAL' | 'ACCEPTABLE' | 'UNSUITABLE';
export type DayPart = 'DAWN' | 'DAY' | 'DUSK' | 'NIGHT';
export type SessionAlertType = 'FLIGHT_REMINDER' | 'SESSION_NO_LONGER_SUITABLE' | 'FINISH_FLIGHT';

export interface ForecastHour {
  timestamp: string;
  temperature: number;
  windSpeed: number;
  precipitation: number;
  dayPart: DayPart;
  suitability: HourSuitability | null;
}

export interface ScheduledSession {
  startTime: string;
  endTime: string;
}

export interface SessionAlert {
  type: SessionAlertType;
  message: string;
}
