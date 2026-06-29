import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule, DateAdapter, MAT_DATE_FORMATS } from '@angular/material/core';
import { ForecastService } from '../../services/forecast.service';
import { DayPart, ForecastHour } from '../../models';
import { toIsoDateTime, maskTimeInput } from '../../utils/datetime';
import { CustomDateAdapter, CUSTOM_DATE_FORMATS } from '../../utils/custom-date-adapter';

@Component({
  selector: 'app-set-weather',
  standalone: true,
  imports: [
    CommonModule, FormsModule,
    MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule,
    MatDatepickerModule, MatNativeDateModule,
  ],
  providers: [
    { provide: DateAdapter, useClass: CustomDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS },
  ],
  templateUrl: './set-weather.component.html',
  styleUrls: ['./set-weather.component.scss'],
})
export class SetWeatherComponent {
  dayParts: DayPart[] = ['DAWN', 'DAY', 'DUSK', 'NIGHT'];

  date: Date | null = null;
  time = '';
  temperature = 20;
  windSpeed = 2;
  precipitation = 0;
  dayPart: DayPart = 'DAY';
  error: string | null = null;

  constructor(
    private dialogRef: MatDialogRef<SetWeatherComponent>,
    private forecastService: ForecastService,
    private cdr: ChangeDetectorRef,
  ) {}

  onNumericInput(event: Event, allowNegative: boolean) {
    const input = event.target as HTMLInputElement;
    let value = allowNegative
      ? input.value.replace(/[^0-9.-]/g, '').replace(/(?!^)-/g, '')
      : input.value.replace(/[^0-9.]/g, '');
    value = value.replace(/(\..*)\./g, '$1');
    input.value = value;
  }

  onTimeInput(event: Event) { maskTimeInput(event); }

  apply() {
    if (!this.date || !this.time) return;
    this.error = null;
    const hour: ForecastHour = {
      timestamp: toIsoDateTime(this.date, this.time),
      temperature: Number(this.temperature),
      windSpeed: Number(this.windSpeed),
      precipitation: Number(this.precipitation),
      dayPart: this.dayPart,
      suitability: null,
    };
    this.forecastService.insertForecastHour(hour).subscribe({
      next: () => this.dialogRef.close(true),
      error: (e) => { this.error = e.error?.message || 'Failed to update forecast hour.'; this.cdr.detectChanges(); },
    });
  }

  cancel() {
    this.dialogRef.close();
  }
}
