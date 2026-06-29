import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SessionAlert } from '../../models';

@Component({
    selector: 'app-alert',
    standalone: true,
    imports: [CommonModule, MatDialogModule, MatButtonModule],
    templateUrl: './alert.component.html',
    styleUrls: ['./alert.component.scss'],
})
export class AlertComponent {
    constructor(@Inject(MAT_DIALOG_DATA) public data: SessionAlert) {}
}
