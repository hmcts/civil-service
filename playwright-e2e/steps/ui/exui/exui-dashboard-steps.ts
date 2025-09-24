import BaseExui from '../../../base/base-exui';
import { AllMethodsStep } from '../../../decorators/test-steps';
import User from '../../../models/user';

@AllMethodsStep()
export default class ExuiDashboardSteps extends BaseExui {
  async AcceptCookies() {
    await this.exuiDashboardActions.acceptCookies();
  }

  async SaveCookies(user: User) {
    await this.exuiDashboardActions.saveCookies(user);
  }

  async GoToCaseList() {
    await this.exuiDashboardActions.goToCaseList();
  }

  async GoToCaseDetails() {
    await this.exuiDashboardActions.goToCaseDetails();
  }

  async SignOut() {
    await this.exuiDashboardActions.signOut();
  }
}
