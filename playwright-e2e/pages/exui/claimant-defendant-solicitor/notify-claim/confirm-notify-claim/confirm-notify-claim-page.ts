import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import ExuiPage from '../../../exui-page/exui-page';
import { confirmationHeading, paragraphs } from './confirm-notify-claim-content';

@AllMethodsStep()
export default class ConfirmNotifyClaimPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.expectHeading(confirmationHeading),
      super.expectText(paragraphs.descriptionText1),
      super.expectText(paragraphs.descriptionText2, { exact: false }),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
