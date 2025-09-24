import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ExuiPage from '../../../../exui-page/exui-page';
import { labels, radioButtons } from './upload-particulars-of-claim-content';

@AllMethodsStep()
export default class UploadParticularsOfClaimPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.verifyHeadings(),
      super.expectLegend(radioButtons.uploadParticularsOfClaim.label),
      super.expectRadioYesLabel(radioButtons.uploadParticularsOfClaim.yes.selector),
      super.expectRadioNoLabel(radioButtons.uploadParticularsOfClaim.no.selector),
    ]);
  }

  async selectYes() {
    await super.clickBySelector(radioButtons.uploadParticularsOfClaim.yes.selector);
  }

  async selectNo() {
    await super.clickBySelector(radioButtons.uploadParticularsOfClaim.no.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
