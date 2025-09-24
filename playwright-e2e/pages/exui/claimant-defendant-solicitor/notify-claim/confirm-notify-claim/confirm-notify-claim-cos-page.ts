import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import ExuiPage from '../../../exui-page/exui-page';
import CCDCaseData from '../../../../../models/ccd/ccd-case-data';
import { confirmationHeadingCOS, paragraphs } from './confirm-notify-claim-content';

@AllMethodsStep()
export default class ConfirmNotifyClaimCOSPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      // super.verifyHeadings(ccdCaseData),
      super.expectHeading(confirmationHeadingCOS),
      super.expectText(paragraphs.descriptionTextCOS1, { exact: false }),
      super.expectText(paragraphs.descriptionTextCOS2),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
