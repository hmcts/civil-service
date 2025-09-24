import { AllMethodsStep } from '../../../decorators/test-steps';
import User from '../../../models/user';
import UserStateHelper from '../../../helpers/users-state-helper';
import BaseApi from '../../../base/base-api';

@AllMethodsStep()
export default class IdamApiSteps extends BaseApi {
  async SetupUsersData(users: User[]) {
    for (const user of users) {
      await this.setupUserData(user);
    }
    UserStateHelper.addUsersToState(users);
  }

  async SetupUserData(user: User) {
    await this.setupUserData(user);
    UserStateHelper.addUserToState(user);
  }
}
