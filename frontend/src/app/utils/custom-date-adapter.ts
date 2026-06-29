import { NativeDateAdapter, MatDateFormats } from '@angular/material/core';
import { Injectable } from '@angular/core';

@Injectable()
export class CustomDateAdapter extends NativeDateAdapter {
    override format(date: Date): string {
        const day = String(date.getDate()).padStart(2, '0');
        const month = String(date.getMonth() + 1).padStart(2, '0');
        return `${day}.${month}.${date.getFullYear()}.`;
    }

    override parse(value: unknown): Date | null {
        if (typeof value !== 'string') return super.parse(value);
        const digits = value.replace(/[^0-9]/g, '');
        if (digits.length !== 8) return null;
        const day = Number(digits.slice(0, 2));
        const month = Number(digits.slice(2, 4));
        const year = Number(digits.slice(4, 8));
        const date = new Date(year, month - 1, day);
        return isNaN(date.getTime()) ? null : date;
    }
}

export const CUSTOM_DATE_FORMATS: MatDateFormats = {
    parse: { dateInput: 'ddMMyyyy' },
    display: {
        dateInput: 'ddMMyyyy',
        monthYearLabel: { year: 'numeric', month: 'short' },
        dateA11yLabel: { year: 'numeric', month: 'long', day: 'numeric' },
        monthYearA11yLabel: { year: 'numeric', month: 'long' },
    },
};
