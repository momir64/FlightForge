import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule, DateAdapter, MAT_DATE_FORMATS } from '@angular/material/core';
import { ForecastService } from '../../services/forecast.service';
import { isoToDate, isoToDisplayTime, toIsoDateTime, maskTimeInput } from '../../utils/datetime';
import { CustomDateAdapter, CUSTOM_DATE_FORMATS } from '../../utils/custom-date-adapter';

@Component({
  selector: 'app-set-time',
  standalone: true,
  imports: [
    CommonModule, FormsModule,
    MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule,
    MatDatepickerModule, MatNativeDateModule,
  ],
  providers: [
    { provide: DateAdapter, useClass: CustomDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS },
  ],
  templateUrl: './set-time.component.html',
  styleUrls: ['./set-time.component.scss'],
})
export class SetTimeComponent implements OnInit {
  date: Date | null = null;
  time = '';
  error: string | null = null;

  constructor(
    private dialogRef: MatDialogRef<SetTimeComponent>,
    private forecastService: ForecastService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit() {
    this.forecastService.getClock().subscribe({
      next: current => {
        this.date = isoToDate(current);
        this.time = isoToDisplayTime(current);
        this.cdr.detectChanges();
      },
    });
  }

  onTimeInput(event: Event) { maskTimeInput(event); }

  apply() {
    if (!this.date || !this.time) return;
    this.error = null;
    this.forecastService.setClock(toIsoDateTime(this.date, this.time)).subscribe({
      next: () => this.dialogRef.close(true),
      error: (e) => { this.error = e.error?.message || 'Failed to set demo clock.'; this.cdr.detectChanges(); },
    });
  }

  cancel() {
    this.dialogRef.close();
  }
}
