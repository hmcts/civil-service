import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import { heading, radioButtons } from './single-claimant-content';

@AllMethodsStep()
export default class SingleClaimantPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectHeading(heading),
      super.expectLabel(radioButtons.markPaidConsent.label),
    ]);
  }

  async selectYes() {
    await super.clickBySelector(radioButtons.markPaidConsent.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
