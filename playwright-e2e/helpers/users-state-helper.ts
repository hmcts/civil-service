import User from '../models/user';
import FileSystemHelper from './file-system-helper';
import FileType from '../enums/file-type';
import UserKey from '../enums/user-key';
import filePaths from '../config/file-paths';

export default class UserStateHelper {
  private static getUserStatePath = (userKey: UserKey) => `${filePaths.users}/${userKey}-user.json`;

  private static getUsersStatePath = (userKey: UserKey) =>
    `${filePaths.users}/${userKey}-users.json`;

  static addUsersToState = (users: User[]) => {
    const usersStateExists = this.usersStateExists(users);
    FileSystemHelper.writeFile(users, this.getUserStatePath(users[0].key), FileType.JSON);
    console.log(
      `Users with key: ${users[0].key} ${usersStateExists ? 'successfully updated' : 'successfully created'}`,
    );
  };

  static addUserToState = (user: User) => {
    const userStateExists = this.userStateExists(user);
    FileSystemHelper.writeFile(user, this.getUserStatePath(user.key), FileType.JSON);
    console.log(
      `User with key: ${user.key} ${userStateExists ? 'successfully updated' : 'successfully created'}`,
    );
  };

  static getUserFromState = ({ key: userKey }: User): User => {
    let user: User;
    try {
      user = FileSystemHelper.readFile(this.getUserStatePath(userKey), FileType.JSON);
      return user;
    } catch {
      return null;
    }
  };

  static getUsersFromState = ([{ key: userKey }]: User[]): User[] => {
    let users: User[];
    try {
      users = FileSystemHelper.readFile(this.getUsersStatePath(userKey), FileType.JSON);
      return users;
    } catch {
      return null;
    }
  };

  static userStateExists = ({ key: userKey }: User) => {
    return FileSystemHelper.exists(this.getUserStatePath(userKey));
  };

  static usersStateExists = ([{ key: userKey }]: User[]) => {
    return FileSystemHelper.exists(this.getUsersStatePath(userKey));
  };

  static deleteUserState = ({ key: userKey }: User) => {
    FileSystemHelper.delete(this.getUserStatePath(userKey));
  };

  static deleteUsersState = ([{ key: userKey }]: User[]) => {
    FileSystemHelper.delete(this.getUsersStatePath(userKey));
  };

  static deleteAllUsersState = () => {
    FileSystemHelper.delete(`${filePaths.users}/`);
  };
}
