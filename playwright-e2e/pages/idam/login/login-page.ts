import urls from '../../../config/urls';
import User from '../../../models/user';
import BasePage from '../../../base/base-page';
import { inputs, buttons } from './login-page-content';
import { AllMethodsStep } from '../../../decorators/test-steps';
import config from '../../../config/config';

@AllMethodsStep({ methodNamesToIgnore: ['login'] })
export default class LoginPage extends BasePage {
  async verifyContent() {
    await super.runVerifications([
      // super.expectText(heading),
      super.expectLabel(inputs.email.label),
      super.expectLabel(inputs.password.label),
    ]);
  }

  private async login({ name, email, password }: User) {
    console.log(`Authenticating user: ${name} by Idam`);
    await super.inputText(email, inputs.email.selector);
    await super.inputSensitiveText(password, inputs.password.selector);
    await super.clickBySelector(buttons.submit.selector);
  }

  async openManageCase() {
    await super.retryGoTo(
      urls.manageCase,
      () =>
        super.expectLabel(inputs.email.label, { timeout: config.playwright.shortExpectTimeout }),
      undefined,
      { retries: 2 },
    );
  }

  async citizenLogin(user: User) {
    await this.login(user);
    await super.expectUrlEnd(['/dashboard', '/eligibility']);
  }

  async manageCaseLogin(user: User) {
    await super.retryAction(
      async () => this.login(user),
      async () => {
        if (!user.wa)
          await super.expectUrlEnd('/cases', { timeout: config.exui.pageSubmitTimeout });
        else
          await super.expectUrlEnd('/work/my-work/list', {
            message: `User: ${user.email} has WA enabled`,
            timeout: config.exui.pageSubmitTimeout,
          });
      },
      async () => {
        await this.openManageCase();
      },
      { retries: 2, message: `Login for user: ${user.name} failed, trying again` },
    );
  }
}
