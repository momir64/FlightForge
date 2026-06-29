import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { SettingsService } from '../../services/settings.service';
import { Settings } from '../../models';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [
    CommonModule, FormsModule,
    MatDialogModule, MatFormFieldModule, MatInputModule,
    MatButtonModule,
  ],
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss'],
})
export class SettingsComponent {
  settings: Settings;

  constructor(
    private dialogRef: MatDialogRef<SettingsComponent>,
    private settingsService: SettingsService,
  ) {
    this.settings = { ...this.settingsService.settings$.value };
  }

  onNumericInput(event: Event) {
    const input = event.target as HTMLInputElement;
    input.value = input.value
      .replace(/[^0-9]/g, '')
      .replace(/^0+(?=\d)/, '');
  }

  onDecimalInput(event: Event) {
    const input = event.target as HTMLInputElement;
    input.value = input.value
      .replace(/[^0-9.]/g, '')
      .replace(/(\..*)\./g, '$1')
      .replace(/^0+(?=\d)/, '');
  }

  save() {
    this.settingsService.update(this.settings);
    this.dialogRef.close();
  }

  cancel() {
    this.dialogRef.close();
  }
}
