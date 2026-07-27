import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd-case-data.ts';
import ExuiPage from '../../../../../mixin-pages/exui-page/exui-page.ts';
import { radioButtons } from './disability-premium-payments-content.ts';

@AllMethodsStep()
export default class DisabilityPremiumPaymentsPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectSelector(radioButtons.disabilityPremium.no.selector),
      super.expectSelector(radioButtons.disabilityPremium.yes.selector),
    ]);
  }

  async selectNo() {
    await super.clickBySelector(radioButtons.disabilityPremium.no.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
