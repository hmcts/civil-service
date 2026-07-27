import { test } from '../playwright-fixtures/index.ts';
import filePaths from '../config/file-paths.ts';
import FileType from '../constants/test-utils/file-type.ts';
import Cookie from '../models/test-utils/cookie.ts';
import FileSystemHelper from './file-system-helper.ts';
import User from '../models/users/user.ts';
import FileError from '../errors/file-error.ts';

export default class CookiesHelper {
  static async getCookies({ email, cookiesPath }: User, isTeardown = false): Promise<Cookie[]> {
    try {
      return FileSystemHelper.readFile(cookiesPath, FileType.JSON);
    } catch (error) {
      if (isTeardown) test.skip(error.message);
      else throw new FileError(`Cookies path: ${cookiesPath}, does not exist for user: ${email}`);
    }
  }

  static async cookiesExist({ cookiesPath }: User) {
    if (!cookiesPath) return false;
    return FileSystemHelper.exists(cookiesPath);
  }

  static async writeCookies(cookies: Cookie[], { cookiesPath }: User) {
    await FileSystemHelper.writeFileAsync(cookies, cookiesPath, FileType.JSON);
  }

  static deleteAllCookies() {
    FileSystemHelper.delete(`${filePaths.userCookies}/`);
  }
}
