import BasePage from '../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import ExuiPage from '../../../../exui-page/exui-page.ts';
import CCDCaseData from '../../../../../../models/ccd/ccd-case-data.ts';
import { radioButtons, subheadings } from './payment-confirmation-spec-content.ts';

@AllMethodsStep()
export default class PaymentConfirmationSpecPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectSubheading(subheadings.fixedCosts),
      super.expectText(radioButtons.label),
    ]);
  }

  async selectYes() {
    await super.clickBySelector(radioButtons.yes.selector);
  }

  async selectNo() {
    await super.clickBySelector(radioButtons.no.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
