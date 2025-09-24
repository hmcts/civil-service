import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import ExuiPage from '../../../exui-page/exui-page';
import { confirmationHeadingCOS, paragraphs } from './confirm-notify-claim-details-content';

@AllMethodsStep()
export default class ConfirmNotifyClaimDetailsCOSPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.expectHeading(confirmationHeadingCOS),
      super.expectText(paragraphs.descriptionTextCOS1),
      super.expectText(paragraphs.descriptionTextCOS2),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
