import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../../models/ccd/ccd-case-data';
import ExuiPage from '../../../../exui-page/exui-page';
import { confirmationHeading } from './confirm-add-defendant-litigation-friend-content';

@AllMethodsStep()
export default class ConfirmAddDefendantLitigationFriendPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectHeading(confirmationHeading),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
