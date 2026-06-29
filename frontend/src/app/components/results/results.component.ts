import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BuildService } from '../../services/build.service';
import { BuildResult, Maneuverability } from '../../models';

@Component({
  selector: 'app-results',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './results.component.html',
  styleUrls: ['./results.component.scss'],
})
export class ResultsComponent {
  result: BuildResult | null = null;

  constructor(
    private buildService: BuildService,
    private cdr: ChangeDetectorRef,
  ) {
    this.buildService.result$.subscribe(r => {
      this.result = r;
      this.cdr.markForCheck();
    });
  }

  get b() { return this.result?.build ?? null; }

  fmt(val: number | null | undefined, unit: string): string {
    if (val == null) return '—';
    return `${val.toFixed(2)} ${unit}`;
  }

  fmtVal(val: number | null | undefined): string {
    if (val == null) return '—';
    return val.toFixed(3);
  }

  twClass(tw: number | null | undefined): string {
    if (tw == null) return '';
    if (tw >= 1.5) return 'good';
    if (tw >= 1.0) return 'warn';
    return 'bad';
  }

  maneuvClass(m: Maneuverability | null | undefined): string {
    if (!m) return '';
    return `maneuv-${m}`;
  }
}
