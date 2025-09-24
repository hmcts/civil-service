import BasePage from '../../../../../../base/base-page';
import { defendantSolicitor1User } from '../../../../../../config/users/exui-users';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ExuiPage from '../../../../exui-page/exui-page';
import { inputs } from './defendant-solicitor-email-content';

@AllMethodsStep()
export default class DefendantSolicitorEmailPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([super.verifyHeadings(), super.expectLabel(inputs.email.label)]);
  }

  async enterEmail() {
    await super.inputText(defendantSolicitor1User.email, inputs.email.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
