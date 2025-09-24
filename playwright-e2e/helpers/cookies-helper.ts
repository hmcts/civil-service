import { test } from '../playwright-fixtures/index';
import filePaths from '../config/file-paths';
import FileType from '../enums/file-type';
import Cookie from '../models/cookie';
import FileSystemHelper from './file-system-helper';
import User from '../models/user';
import FileError from '../errors/file-error';

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
