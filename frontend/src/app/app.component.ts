import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { TopbarComponent } from './components/topbar/topbar.component';
import { PartsComponent } from './components/parts/parts.component';
import { ResultsComponent } from './components/results/results.component';
import { ForecastComponent } from './components/forecast/forecast.component';
import { AlertComponent } from './dialogs/alert/alert.component';
import { SettingsComponent } from './dialogs/settings/settings.component';
import { SetTimeComponent } from './dialogs/set-time/set-time.component';
import { SetWeatherComponent } from './dialogs/set-weather/set-weather.component';
import { ComponentService } from './services/component.service';
import { ForecastService } from './services/forecast.service';
import { WebsocketService } from './services/websocket.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    TopbarComponent,
    PartsComponent,
    ResultsComponent,
    ForecastComponent,
  ],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit {
  private alertRef: MatDialogRef<AlertComponent> | null = null;

  constructor(
    private componentService: ComponentService,
    private forecastService: ForecastService,
    private websocketService: WebsocketService,
    private dialog: MatDialog,
  ) {}

  ngOnInit() {
    this.componentService.loadAll().subscribe();
    this.websocketService.connect();

    this.websocketService.alerts$.subscribe(alerts => {
      if (alerts.length > 0 && !this.alertRef) {
        this.alertRef = this.dialog.open(AlertComponent, {
          data: alerts[0],
          disableClose: true,
          width: '420px',
        });
        this.alertRef.afterClosed().subscribe(() => {
          this.alertRef = null;
          this.websocketService.dismissFirst();
        });
      }
    });
  }

  openSettings() {
    this.dialog.open(SettingsComponent, { width: '380px' });
  }

  openSetTime() {
    this.dialog.open(SetTimeComponent, { width: '380px' })
      .afterClosed().subscribe(result => { if (result) this.reloadForecastData(); });
  }

  openSetWeather() {
    this.dialog.open(SetWeatherComponent, { width: '420px' })
      .afterClosed().subscribe(result => { if (result) this.reloadForecastData(); });
  }

  private reloadForecastData() {
    this.forecastService.loadForecast().subscribe();
    this.forecastService.loadSessions().subscribe();
  }
}
