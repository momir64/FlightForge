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
import { ComponentService } from './services/component.service';
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
  template: `
    <div class="app-shell">
      <div class="content-wrap">
        <div class="header">
          <span class="logo"><span class="material-symbols-rounded logo-icon">travel</span> FlightForge</span>
          <button mat-icon-button class="settings-btn" (click)="openSettings()" title="Settings">
            <span class="material-symbols-rounded">settings</span>
          </button>
        </div>
        <app-topbar class="card topbar-card" />
        <div class="main-area">
          <app-parts class="card panel-card" />
          <app-results class="card panel-card" />
        </div>
        <app-forecast class="card forecast-card" />
      </div>
    </div>
  `,
  styles: [`
    .app-shell {
      display: flex;
      flex-direction: column;
      height: 100vh;
      align-items: center;
      overflow: auto;
      padding-bottom: 24px;
    }

    .header {
      display: flex;
      align-items: center;
      justify-content: flex-start;
      width: 100%;
      padding: 24px 0 6px;
      flex-shrink: 0;
      position: relative;
    }

    .logo {
      font-family: var(--mono), monospace;
      font-size: 20px;
      font-weight: 600;
      color: #fff;
      letter-spacing: 0.05em;
      display: flex;
      align-items: center;
      gap: 6px;
    }

    .logo-icon {
      font-size: 24px !important;
      color: #fff;
    }

    .settings-btn {
      position: absolute;
      right: 0;
      top: 50%;
      transform: translateY(-50%);
      color: var(--text-dim) !important;

      .material-symbols-rounded {
        font-size: 24px !important;
      }
    }

    .content-wrap {
      display: flex;
      flex-direction: column;
      gap: 12px;
      width: 100%;
      max-width: 1140px;
      margin: 0 auto;
      padding: 0 24px;
    }

    .topbar-card {
      flex-shrink: 0;
    }

    .main-area {
      display: flex;
      gap: 12px;
    }

    .panel-card {
      flex: 1;
      min-width: 0;
    }

    .forecast-card {
      flex-shrink: 0;
    }
  `],
})
export class AppComponent implements OnInit {
  private alertRef: MatDialogRef<AlertComponent> | null = null;

  constructor(
    private componentService: ComponentService,
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
}
