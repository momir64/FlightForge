// Wire format sent to the backend is ISO (yyyy-MM-ddTHH:mm). The date part comes from a
// Date (mat-datepicker), the time part from a masked dd.MM.yyyy-style HH:mm text field.

export function isoToDate(iso: string): Date {
  const [y, m, d] = iso.substring(0, 10).split('-').map(Number);
  return new Date(y, m - 1, d);
}

export function isoToDisplayTime(iso: string): string {
  return iso.substring(11, 16);
}

export function toIsoDateTime(date: Date, displayTime: string): string {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}T${displayTime}`;
}

// Builds HH:mm digit-by-digit, clamping each segment so it can never become an
// out-of-range hour (>23) or minute (>59) — e.g. typing "3" for the hour auto-pads
// to "03" instead of waiting for a second digit that would make "3X" invalid.
export function maskTimeInput(event: Event) {
  const input = event.target as HTMLInputElement;
  const digits = input.value.replace(/[^0-9]/g, '').slice(0, 4);

  let hour = '';
  let minute = '';
  let i = 0;

  if (i < digits.length) {
    const d0 = digits[i++];
    if (d0 > '2') {
      hour = '0' + d0;
    } else {
      hour = d0;
      if (i < digits.length) {
        const candidate = hour + digits[i];
        if (Number(candidate) <= 23) { hour = candidate; i++; }
        else { hour = '0' + hour; }
      }
    }
  }

  if (i < digits.length) {
    const d0 = digits[i++];
    if (d0 > '5') {
      minute = '0' + d0;
    } else {
      minute = d0;
      if (i < digits.length) { minute += digits[i++]; }
    }
  }

  input.value = minute ? `${hour}:${minute}` : hour;
}
