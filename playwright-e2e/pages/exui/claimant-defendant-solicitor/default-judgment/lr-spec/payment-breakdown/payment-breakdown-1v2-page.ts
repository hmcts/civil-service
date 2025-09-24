import BasePage from '../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../../exui-page/exui-page.ts';
import { subheadings, amountHeadings } from './payment-breakdown-content.ts';

@AllMethodsStep()
export default class PaymentBreakdown1v2Page extends ExuiPage(BasePage) {
  async verifyContent(cddCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(cddCaseData),
      super.expectText(amountHeadings.claim),
      super.expectText(amountHeadings.fixedCost),
      super.expectText(amountHeadings.claimFee),
      super.expectText(subheadings.subtotal),
      super.expectText(subheadings.totalStillOwed),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
