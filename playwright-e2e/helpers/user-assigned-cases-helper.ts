import config from '../config/config';
import filePaths from '../config/file-paths';
import FileType from '../enums/file-type';
import UserKey from '../enums/user-key';
import User from '../models/user';
import FileSystemHelper from './file-system-helper';

//TODO: Could be a potentially concurrency issue when storing assigned caseIds for users when multiple workers are running but will assess and fix later.
//e.g. Two workers could be updating caseIds for a user at the same time.

export default class UserAssignedCasesHelper {
  private static getUserAssignedCasesPath = (userKey: UserKey) =>
    `${filePaths.userAssignedCases}/${userKey}.json`;

  static async getUserAssignedCases({ key: userKey }: User): Promise<number[] | null> {
    if (config.unassignCases) {
      try {
        const unassignedCases = FileSystemHelper.readFile(
          this.getUserAssignedCasesPath(userKey),
          FileType.JSON,
        );
        return unassignedCases;
      } catch {
        return null;
      }
    }
    return null;
  }

  static async addAssignedCaseToUser(user: User, caseId: number) {
    if (config.unassignCases) {
      console.log(`Adding caseId: ${caseId} to user assigned cases for user: ${user.name}`);
      const userAssignedCases = (await this.getUserAssignedCases(user)) ?? [];
      userAssignedCases.push(caseId);
      await FileSystemHelper.writeFileAsync(
        userAssignedCases,
        this.getUserAssignedCasesPath(user.key),
        FileType.JSON,
      );
      console.log(
        `Added caseId: ${caseId} to user assigned cases for user: ${user.name} successfully`,
      );
    }
  }

  static async deleteUserAssignedCases({ key: userKey }: User) {
    FileSystemHelper.delete(this.getUserAssignedCasesPath(userKey));
  }

  static async deleteAllUsersAssignedCases() {
    FileSystemHelper.delete(`${filePaths.userAssignedCases}/`);
  }
}
