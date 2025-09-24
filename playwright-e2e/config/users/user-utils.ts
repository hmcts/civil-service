import UserKey from '../../enums/user-key';
import UserStateHelper from '../../helpers/users-state-helper';
import User from '../../models/user';
import filePaths from '../file-paths';

let userKeysBeingUsed = new Set<UserKey>();

export const getUser = (user: User): User => {
  if (!userKeysBeingUsed.has(user.key)) {
    userKeysBeingUsed.add(user.key);
    return (
      UserStateHelper.getUserFromState(user) ?? {
        ...user,
        cookiesPath: `${filePaths.userCookies}/${user.key}.json`,
      }
    );
  }
  throw new Error(`Cannot have multiple users or user arrays with key: ${user.key}`);
};

export const getUsers = (users: User[]): User[] => {
  if (!userKeysBeingUsed.has(users[0].key)) {
    userKeysBeingUsed.add(users[0].key);
    return (
      UserStateHelper.getUsersFromState(users) ??
      users.map((user, index) => ({
        ...user,
        cookiesPath: `${filePaths.userCookies}/${user.key}-${index}.json`,
      }))
    );
  }
  throw new Error(`Cannot have multiple user or user arrays with key: ${users[0].key}`);
};

export const clearUserKeysBeingUsed = () => (userKeysBeingUsed = undefined);
