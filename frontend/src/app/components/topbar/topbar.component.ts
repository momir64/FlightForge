import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { ComponentService } from '../../services/component.service';
import { StateService, TopbarState } from '../../services/state.service';
import { AirplaneSpecs, Priority } from '../../models';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [
    CommonModule, FormsModule,
    MatSelectModule, MatInputModule, MatFormFieldModule,
    MatAutocompleteModule,
  ],
  templateUrl: './topbar.component.html',
  styleUrls: ['./topbar.component.scss'],
})
export class TopbarComponent {
  airplanes: AirplaneSpecs[] = [];
  filteredAirplanes: AirplaneSpecs[] = [];
  state: TopbarState;
  selectedAirplaneId: number | null = null;

  priorities = [
    { value: 'MIN_PRICE' as Priority, label: 'Min price' },
    { value: 'MIN_WEIGHT' as Priority, label: 'Min weight' },
    { value: 'MAX_FLIGHT_TIME' as Priority, label: 'Max flight time' },
    { value: 'MAX_TW_FACTOR' as Priority, label: 'Max T/W factor' },
  ];

  gearOptions = [
    { value: false, label: 'Any' },
    { value: true, label: 'Metal only' },
  ];

  constructor(
    private componentService: ComponentService,
    private stateService: StateService,
    private cdr: ChangeDetectorRef,
  ) {
    this.state = this.stateService.topbar$.value;
    this.stateService.topbar$.subscribe(s => {
      this.state = s;
      this.cdr.markForCheck();
    });
    this.componentService.airplanes$.subscribe(a => {
      this.airplanes = a;
      this.filteredAirplanes = a;
      // Restore the persisted airplane only once the list backing displayWith() is
      // populated — assigning it before that means the autocomplete input never
      // re-renders later, since the bound value itself never changes again.
      if (this.selectedAirplaneId == null) {
        const id = this.stateService.topbar$.value.airplaneId;
        if (id != null && a.some(x => x.id === id)) this.selectedAirplaneId = id;
      }
      this.cdr.markForCheck();
    });
  }

  patch(field: keyof TopbarState, value: unknown) {
    this.stateService.patchTopbar({ [field]: value });
  }

  airplaneDisplayFn = (id: number): string => {
    if (!id) return '';
    const a = this.airplanes.find(x => x.id === id);
    return a ? a.name : '';
  };

  onAirplaneInput(event: Event) {
    const query = (event.target as HTMLInputElement).value.toLowerCase();
    this.filteredAirplanes = query
      ? this.airplanes.filter(a => a.name.toLowerCase().includes(query))
      : this.airplanes;
  }

  onAirplaneFocus() {
    this.filteredAirplanes = this.airplanes;
  }

  onAirplaneSelected(id: number) {
    this.selectedAirplaneId = id;
    this.patch('airplaneId', id);
  }

  onAirplaneBlur(event: Event) {
    const value = (event.target as HTMLInputElement).value;
    if (value !== this.airplaneDisplayFn(this.selectedAirplaneId!)) {
      this.selectedAirplaneId = null;
      this.patch('airplaneId', null);
    }
  }

  onNumericInput(event: Event, allowFloat: boolean) {
    const input = event.target as HTMLInputElement;
    if (allowFloat) {
      input.value = input.value
        .replace(/[^0-9.]/g, '')
        .replace(/(\..*)\./g, '$1')
        .replace(/^0+(?=\d)/, '');
    } else {
      input.value = input.value
        .replace(/[^0-9]/g, '')
        .replace(/^0+(?=\d)/, '');
    }
  }
}
