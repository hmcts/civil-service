import { bankHolidays } from '../config/data';
import DateStringFormats from '../models/date-string-formats';
export default class DateHelper {
  private static shortMonths: string[] = [
    'Jan',
    'Feb',
    'Mar',
    'Apr',
    'May',
    'Jun',
    'Jul',
    'Aug',
    'Sep',
    'Oct',
    'Nov',
    'Dec',
  ];

  private static longMonths = [
    'January',
    'February',
    'March',
    'April',
    'May',
    'June',
    'July',
    'August',
    'September',
    'October',
    'November',
    'December',
  ];

  private static addDate(date: Date, { days = 0, months = 0, years = 0, workingDay = false }) {
    date.setDate(date.getDate() + days);
    date.setMonth(date.getMonth() + months);
    date.setFullYear(date.getFullYear() + years);
    if (workingDay) {
      date = this.getNextWorkingDay(date);
    }
    return date;
  }

  static getToday(): Date {
    return new Date();
  }

  static addToToday({
    days = 0,
    months = 0,
    years = 0,
    workingDay = false,
    addDayAfter4pm = false,
  }): Date {
    const now = new Date();
    if (addDayAfter4pm && this.isAfter4pm(now)) {
      days += 1;
    }
    return this.addDate(now, { days, months, years, workingDay });
  }

  static addToDate(
    date: string,
    { days = 0, months = 0, years = 0, workingDay = false, addDayAfter4pm = false },
  ): Date {
    if (addDayAfter4pm && this.isAfter4pm(new Date())) {
      days += 1;
    }
    return this.addDate(new Date(date), { days, months, years, workingDay });
  }

  private static subtractDate(
    date: Date,
    { days = 0, months = 0, years = 0, workingDay = false },
  ): Date {
    date.setDate(date.getDate() - days);
    date.setMonth(date.getMonth() - months);
    date.setFullYear(date.getFullYear() - years);

    if (workingDay) {
      date = this.getNextWorkingDay(date);
    }
    return date;
  }

  static subtractFromToday({
    days = 0,
    months = 0,
    years = 0,
    workingDay = false,
    addDayAfter4pm = false,
  }): Date {
    const now = new Date();
    if (addDayAfter4pm && this.isAfter4pm(now)) {
      days -= 1;
    }
    return this.subtractDate(now, { days, months, years, workingDay });
  }

  static subtractFromDate(
    date: string,
    { days = 0, months = 0, years = 0, workingDay = false, addDayAfter4pm = false },
  ): Date {
    if (addDayAfter4pm && this.isAfter4pm(new Date())) {
      days -= 1;
    }
    return this.subtractDate(new Date(date), { days, months, years, workingDay });
  }

  static getTwoDigitDay(date: Date): string {
    const day = date.getDate();
    return day < 10 ? `0${day}` : `${day}`;
  }

  static getTwoDigitMonth(date: Date): string {
    const month = date.getMonth() + 1;
    return month < 10 ? `0${month}` : `${month}`;
  }

  static getTwoDigitYear(date: Date): string {
    const year = date.getFullYear();
    return `${year % 100}`.padStart(2, '0');
  }

  static formatDateToString(
    inputDate: string | Date,
    {
      inputFormat = 'YYYY-MM-DD' as DateStringFormats,
      outputFormat = 'DD Month YYYY' as DateStringFormats,
    } = {},
  ): string {
    let date: Date;
    let dateString: string;

    if (typeof inputDate === 'string') {
      if (inputFormat === 'YYYY-MM-DD') {
        const [year, month, day] = inputDate.split('-');
        date = new Date(parseInt(year), parseInt(month) - 1, parseInt(day));
      } else {
        date = new Date(inputDate);
      }
    } else {
      date = inputDate;
    }

    if (outputFormat === 'DD Month YYYY') {
      dateString = `${date.getDate()} ${this.longMonths[date.getMonth()]} ${date.getFullYear()}`;
    } else if (outputFormat === 'DD Mon YYYY') {
      dateString = `${date.getDate()} ${this.shortMonths[date.getMonth()]} ${date.getFullYear()}`;
    } else if (outputFormat === 'YYYY-MM-DDTHH:MM:SS') {
      dateString = date.toISOString().slice(0, 19);
    }

    return dateString;
  }

  private static isAfter4pm(date: Date) {
    return date.getHours() >= 16;
  }

  private static isWeekend(date: Date): boolean {
    const day = date.getDay();
    return day === 0 || day === 6; // 0 = Sunday, 6 = Saturday
  }

  private static isNonWorkingDay(date: Date): boolean {
    // Check if the date is a weekend
    if (this.isWeekend(date)) return true;

    // Check if the date is a holiday from the list of holidays
    return bankHolidays.some((bankHoliday) => {
      const bankHolidayDate = new Date(bankHoliday);
      return bankHolidayDate.toDateString() === date.toDateString();
    });
  }

  private static getNextWorkingDay(date: Date): Date {
    const nextDate = new Date(date);

    // Increment day by 1 until we find a working day
    while (this.isNonWorkingDay(nextDate)) {
      nextDate.setDate(nextDate.getDate() + 1);
    }

    return nextDate;
  }
}
