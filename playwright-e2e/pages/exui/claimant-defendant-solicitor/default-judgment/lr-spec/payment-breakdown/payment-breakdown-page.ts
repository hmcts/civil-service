import BasePage from '../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import ExuiPage from '../../../../exui-page/exui-page.ts';
import { subheadings, amountHeadings } from './payment-breakdown-content.ts';
import CCDCaseData from '../../../../../../models/ccd/ccd-case-data.ts';

@AllMethodsStep()
export default class PaymentBreakdownPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
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
