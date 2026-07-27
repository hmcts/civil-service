import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import { heading, subheading } from './submit-discontinue-claim-claimant-content';

@AllMethodsStep()
export default class SubmitDiscontinueClaimClaimantPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectHeading(heading),
      super.expectSubheading(subheading),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
