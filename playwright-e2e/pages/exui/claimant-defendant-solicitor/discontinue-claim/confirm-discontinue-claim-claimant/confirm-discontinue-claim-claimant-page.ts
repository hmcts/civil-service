import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import CCDCaseData from '../../../../../models/ccd-case-data';
import { heading, paragraphs } from './confirm-discontinue-claim-claimant-content';

@AllMethodsStep()
export default class ConfirmDiscontinueClaimClaimantPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectHeading(heading),
      super.expectText(paragraphs.paragraph1),
      super.expectText(paragraphs.paragraph2),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
