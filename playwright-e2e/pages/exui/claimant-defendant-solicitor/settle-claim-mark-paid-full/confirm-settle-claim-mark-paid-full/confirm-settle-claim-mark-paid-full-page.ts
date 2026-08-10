import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import { confirmationHeadings } from './confirm-settle-claim-mark-paid-full-content';

@AllMethodsStep()
export default class ConfirmSettleClaimMarkPaidFullPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectText(confirmationHeadings.confirmPaidInFull),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
