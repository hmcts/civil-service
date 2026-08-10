import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd-case-data.ts';
import ExuiPage from '../../../../../mixin-pages/exui-page/exui-page.ts';
import { radioButtons } from './when-will-claim-be-paid-content.ts';

@AllMethodsStep()
export default class WhenWillClaimBePaidPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectLabel(radioButtons.immediately.label, { ignoreDuplicates: true }),
      super.expectLabel(radioButtons.bySetDate.label, { ignoreDuplicates: true }),
      super.expectLabel(radioButtons.repaymentPlan.label, { ignoreDuplicates: true }),
    ]);
  }

  async selectImmediately() {
    await super.clickBySelector(radioButtons.immediately.selector);
  }

  async selectRepaymentPlan() {
    await super.clickBySelector(radioButtons.repaymentPlan.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
