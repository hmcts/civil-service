import BasePage from '../../../../../../../base/base-page.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import { confirmationHeadings, paragraphs } from './confirm-claimant-response-content.ts';

@AllMethodsStep()
export default class ConfirmClaimantResponsePage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectHeading(confirmationHeadings.proceed),
      super.expectText(paragraphs.descriptionText1, { count: 1 }),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
