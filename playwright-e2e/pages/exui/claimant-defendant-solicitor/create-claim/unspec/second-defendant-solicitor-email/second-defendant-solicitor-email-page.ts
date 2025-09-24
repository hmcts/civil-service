import BasePage from '../../../../../../base/base-page';
import { defendantSolicitor2User } from '../../../../../../config/users/exui-users';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ExuiPage from '../../../../exui-page/exui-page';
import { heading, inputs } from './second-defendant-solicitor-email-content';

@AllMethodsStep()
export default class SecondDefendantSolicitorEmailPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.expectHeading(heading),
      super.expectLabel(inputs.email.label),
      super.expectText(inputs.email.hintText),
    ]);
  }

  async enterEmail() {
    await super.inputText(defendantSolicitor2User.email, inputs.email.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
