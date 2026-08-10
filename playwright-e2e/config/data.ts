import BankHolidaysHelper from '../helpers/bank-holidays-helper';

export const bankHolidays = BankHolidaysHelper.getBankHolidaysFromState() ?? [];
