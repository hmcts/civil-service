import filePaths from '../config/file-paths.ts';
import FileType from '../constants/test-utils/file-type.ts';
import FileSystemHelper from './file-system-helper.ts';

export default class BankHolidaysHelper {
  static getBankHolidaysFromState = (): string[] | null => {
    try {
      const bankHolidays = FileSystemHelper.readFile(filePaths.bankHolidaysJson, FileType.JSON);
      return bankHolidays;
    } catch {
      return null;
    }
  };

  static writeBankHolidays(bankHolidays: string[]) {
    FileSystemHelper.writeFile(bankHolidays, filePaths.bankHolidaysJson, FileType.JSON);
  }

  static deleteBankHolidays() {
    FileSystemHelper.delete(filePaths.bankHolidaysJson);
  }
}
