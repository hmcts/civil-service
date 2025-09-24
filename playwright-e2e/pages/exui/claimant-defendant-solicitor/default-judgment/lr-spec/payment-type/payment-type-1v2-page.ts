import BasePage from '../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../../exui-page/exui-page.ts';
import { subheadings, radioButtons } from './payment-type-content.ts';

@AllMethodsStep()
export default class PaymentType1v2Page extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectSubheading(subheadings.paymentType1v2),
      super.expectLegend(radioButtons.paymentType.label),
      super.expectLabel(radioButtons.paymentType.immediately.label),
      super.expectLabel(radioButtons.paymentType.setDate.label),
      super.expectLabel(radioButtons.paymentType.repaymentPlan.label),
    ]);
  }

  async selectImmediatePayment() {
    await super.clickBySelector(radioButtons.paymentType.immediately.selector);
  }

  async selectSetPaymentDate() {
    await super.clickBySelector(radioButtons.paymentType.setDate.selector);
  }

  async selectRepaymentPlan() {
    await super.clickBySelector(radioButtons.paymentType.repaymentPlan.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
