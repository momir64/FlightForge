import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatDialog } from '@angular/material/dialog';
import { ComponentService } from '../../services/component.service';
import { BuildService } from '../../services/build.service';
import { ForecastService } from '../../services/forecast.service';
import { StateService } from '../../services/state.service';
import { Battery, ESC, Motor, MotorConfiguration, Propeller, Servo } from '../../models';
import { ErrorDialogComponent } from '../../dialogs/error/error-dialog.component';

interface MotorOption {
  motor: Motor;
  configs: MotorConfiguration[];
}

interface PropellerOption {
  propeller: Propeller;
  cellCount: number;
  thrust: number;
  configId: number;
}

@Component({
  selector: 'app-parts',
  standalone: true,
  imports: [
    CommonModule, FormsModule,
    MatSelectModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatProgressSpinnerModule, MatAutocompleteModule,
  ],
  templateUrl: './parts.component.html',
  styleUrls: ['./parts.component.scss'],
})
export class PartsComponent {
  motorConfigs: MotorConfiguration[] = [];
  escs: ESC[] = [];
  batteries: Battery[] = [];
  servos: Servo[] = [];

  motorOptions: MotorOption[] = [];
  propellerOptions: PropellerOption[] = [];

  selectedMotorId: number | null = null;
  selectedConfigId: number | null = null;
  escId: number | null = null;
  batteryId: number | null = null;
  servoId: number | null = null;

  filteredMotors: MotorOption[] = [];
  filteredPropellers: PropellerOption[] = [];
  filteredEscs: ESC[] = [];
  filteredBatteries: Battery[] = [];
  filteredServos: Servo[] = [];

  loading = false;
  private autoEvaluated = false;

  constructor(
    private componentService: ComponentService,
    private buildService: BuildService,
    private forecastService: ForecastService,
    private stateService: StateService,
    private cdr: ChangeDetectorRef,
    private dialog: MatDialog,
  ) {
    this.componentService.motorConfigs$.subscribe(v => {
      this.motorConfigs = v;
      this.buildMotorOptions();
      // Restore the persisted motor/propeller selection only once the lists that back
      // displayWith() are populated — assigning the id before that means the autocomplete
      // input never re-renders later, since the bound value itself never changes again.
      if (this.selectedMotorId == null) {
        const persisted = this.stateService.components$.value;
        const configId = persisted.motorConfigurationId;
        const mc = configId != null ? v.find(x => x.id === configId) : undefined;
        if (mc) {
          this.selectedMotorId = mc.motor.id;
          this.updatePropellerOptions();
          this.selectedConfigId = configId;
        } else if (persisted.selectedMotorId != null && this.motorOptions.some(m => m.motor.id === persisted.selectedMotorId)) {
          // Motor was persisted without a propeller selection.
          this.selectedMotorId = persisted.selectedMotorId;
          this.updatePropellerOptions();
        }
      }
      this.cdr.markForCheck();
    });
    this.componentService.escs$.subscribe(v => {
      this.escs = v;
      this.filteredEscs = v;
      if (this.escId == null) {
        const id = this.stateService.components$.value.escId;
        if (id != null && v.some(e => e.id === id)) this.escId = id;
      }
      this.cdr.markForCheck();
    });
    this.componentService.batteries$.subscribe(v => {
      this.batteries = v;
      this.filteredBatteries = v;
      if (this.batteryId == null) {
        const id = this.stateService.components$.value.batteryId;
        if (id != null && v.some(b => b.id === id)) this.batteryId = id;
      }
      this.cdr.markForCheck();
    });
    this.componentService.servos$.subscribe(v => {
      this.servos = v;
      this.filteredServos = v;
      if (this.servoId == null) {
        const id = this.stateService.components$.value.servoId;
        if (id != null && v.some(s => s.id === id)) this.servoId = id;
      }
      if (!this.autoEvaluated && this.canEvaluate) {
        this.autoEvaluated = true;
        this.evaluate();
      }
      this.cdr.markForCheck();
    });
    this.stateService.topbar$.subscribe(() => this.cdr.markForCheck());
  }

  get canEvaluate(): boolean {
    return this.stateService.topbar$.value.airplaneId != null
      && this.selectedConfigId != null
      && this.escId != null
      && this.batteryId != null
      && this.servoId != null;
  }

  get canSuggest(): boolean {
    return this.stateService.topbar$.value.airplaneId != null;
  }

  private buildMotorOptions() {
    const motorMap = new Map<number, MotorOption>();
    for (const mc of this.motorConfigs) {
      const existing = motorMap.get(mc.motor.id);
      if (existing) {
        existing.configs.push(mc);
      } else {
        motorMap.set(mc.motor.id, { motor: mc.motor, configs: [mc] });
      }
    }
    this.motorOptions = [...motorMap.values()];
    this.filteredMotors = this.motorOptions;
  }

  // Display functions — convert id to display string
  motorDisplayFn = (id: number): string => {
    if (!id) return '';
    const m = this.motorOptions.find(x => x.motor.id === id);
    return m ? this.motorLabel(m.motor) : '';
  };

  propellerDisplayFn = (configId: number): string => {
    if (!configId) return '';
    const p = this.propellerOptions.find(x => x.configId === configId);
    return p ? this.propLabel(p) : '';
  };

  escDisplayFn = (id: number): string => {
    if (!id) return '';
    const e = this.escs.find(x => x.id === id);
    return e ? e.name : '';
  };

  batteryDisplayFn = (id: number): string => {
    if (!id) return '';
    const b = this.batteries.find(x => x.id === id);
    return b ? b.name : '';
  };

  servoDisplayFn = (id: number): string => {
    if (!id) return '';
    const s = this.servos.find(x => x.id === id);
    return s ? s.name : '';
  };

  // Unified search input handler
  onSearchInput(event: Event, field: 'motor' | 'propeller' | 'esc' | 'battery' | 'servo') {
    const query = (event.target as HTMLInputElement).value.toLowerCase();
    switch (field) {
      case 'motor':
        this.filteredMotors = query
          ? this.motorOptions.filter(m => m.motor.name.toLowerCase().includes(query))
          : this.motorOptions;
        break;
      case 'propeller':
        this.filteredPropellers = query
          ? this.propellerOptions.filter(p => this.propLabel(p).toLowerCase().includes(query))
          : this.propellerOptions;
        break;
      case 'esc':
        this.filteredEscs = query
          ? this.escs.filter(e => this.escLabel(e).toLowerCase().includes(query))
          : this.escs;
        break;
      case 'battery':
        this.filteredBatteries = query
          ? this.batteries.filter(b => this.batLabel(b).toLowerCase().includes(query))
          : this.batteries;
        break;
      case 'servo':
        this.filteredServos = query
          ? this.servos.filter(s => this.servoLabel(s).toLowerCase().includes(query))
          : this.servos;
        break;
    }
  }

  onFocusReset(field: 'motor' | 'propeller' | 'esc' | 'battery' | 'servo') {
    switch (field) {
      case 'motor': this.filteredMotors = this.motorOptions; break;
      case 'propeller': this.filteredPropellers = this.propellerOptions; break;
      case 'esc': this.filteredEscs = this.escs; break;
      case 'battery': this.filteredBatteries = this.batteries; break;
      case 'servo': this.filteredServos = this.servos; break;
    }
  }

  // If the text left in the field doesn't match the selected option's label,
  // treat it as the user clearing the selection.
  onBlur(event: Event, field: 'motor' | 'propeller' | 'esc' | 'battery' | 'servo') {
    const value = (event.target as HTMLInputElement).value;
    switch (field) {
      case 'motor':
        if (value !== this.motorDisplayFn(this.selectedMotorId!)) {
          this.selectedMotorId = null;
          this.selectedConfigId = null;
          this.updatePropellerOptions();
          this.syncState();
        }
        break;
      case 'propeller':
        if (value !== this.propellerDisplayFn(this.selectedConfigId!)) {
          this.selectedConfigId = null;
          this.syncState();
        }
        break;
      case 'esc':
        if (value !== this.escDisplayFn(this.escId!)) {
          this.escId = null;
          this.syncState();
        }
        break;
      case 'battery':
        if (value !== this.batteryDisplayFn(this.batteryId!)) {
          this.batteryId = null;
          this.syncState();
        }
        break;
      case 'servo':
        if (value !== this.servoDisplayFn(this.servoId!)) {
          this.servoId = null;
          this.syncState();
        }
        break;
    }
  }

  onMotorSelected(motorId: number) {
    this.selectedMotorId = motorId;
    this.selectedConfigId = null;
    this.updatePropellerOptions();
    this.syncState();
  }

  private updatePropellerOptions() {
    if (!this.selectedMotorId) {
      this.propellerOptions = [];
      this.filteredPropellers = [];
      return;
    }
    const configs = this.motorConfigs.filter(mc => mc.motor.id === this.selectedMotorId);
    this.propellerOptions = configs.map(mc => ({
      propeller: mc.propeller,
      cellCount: mc.cellCount,
      thrust: mc.thrust,
      configId: mc.id,
    }));
    this.filteredPropellers = this.propellerOptions;
  }

  onPropellerSelected(configId: number) {
    this.selectedConfigId = configId;
    this.syncState();
  }

  onEscSelected(id: number) { this.escId = id; this.syncState(); }
  onBatterySelected(id: number) { this.batteryId = id; this.syncState(); }
  onServoSelected(id: number) { this.servoId = id; this.syncState(); }

  motorLabel(m: Motor) {
    return `${m.name}${m.available ? '' : ' [out of stock]'}`;
  }

  propLabel(p: PropellerOption) {
    return `${p.propeller.diameter}"×${p.propeller.pitch}" ${p.propeller.bladeCount}B (${p.cellCount}S) — ${p.thrust}g`
      + (p.propeller.available ? '' : ' [out of stock]');
  }

  escLabel(e: ESC) {
    return `${e.name}${e.available ? '' : ' [out of stock]'}`;
  }

  batLabel(b: Battery) {
    return `${b.name} ${b.capacity}mAh ${b.cellCount}S${b.available ? '' : ' [out of stock]'}`;
  }

  servoLabel(s: Servo) {
    return `${s.name} ${s.torque}kg·cm ${s.gearType.toLowerCase()}${s.available ? '' : ' [out of stock]'}`;
  }

  private syncState() {
    this.stateService.patchComponents({
      selectedMotorId: this.selectedMotorId,
      motorConfigurationId: this.selectedConfigId,
      escId: this.escId,
      batteryId: this.batteryId,
      servoId: this.servoId,
    });
  }

  evaluate() {
    this.syncState();
    this.loading = true;
    this.cdr.markForCheck();
    this.buildService.evaluate(this.stateService.buildRequest()).subscribe({
      next: () => {
        this.loading = false;
        this.forecastService.loadForecast().subscribe();
        this.cdr.markForCheck();
      },
      error: (e) => { this.loading = false; this.cdr.markForCheck(); this.showError(e, 'Evaluation failed'); },
    });
  }

  suggest() {
    this.syncState();
    this.loading = true;
    this.cdr.markForCheck();
    this.buildService.suggest(this.stateService.buildRequest()).subscribe({
      next: result => {
        this.loading = false;
        const b = result.build;
        if (b.motorConfiguration) {
          this.selectedMotorId = b.motorConfiguration.motor.id;
          this.updatePropellerOptions();
          this.selectedConfigId = b.motorConfiguration.id;
        }
        this.escId = b.esc?.id ?? null;
        this.batteryId = b.battery?.id ?? null;
        this.servoId = b.servos?.[0]?.id ?? null;
        this.syncState();
        this.forecastService.loadForecast().subscribe();
        this.cdr.markForCheck();
      },
      error: (e) => { this.loading = false; this.cdr.markForCheck(); this.showError(e, 'Auto-suggest failed'); },
    });
  }

  private showError(e: any, fallback: string) {
    const message = e.error?.message || fallback;
    this.dialog.open(ErrorDialogComponent, { data: message, width: '420px' });
  }
}
