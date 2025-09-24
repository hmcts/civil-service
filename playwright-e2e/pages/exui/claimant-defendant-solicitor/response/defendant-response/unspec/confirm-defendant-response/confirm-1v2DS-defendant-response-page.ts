import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import { confirmationHeading, paragraphs } from './confirm-defendant-response-content.ts';

@AllMethodsStep()
export default class Confirm1v2DSDefendantResponsePage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectHeading(confirmationHeading),
      super.expectText(ccdCaseData.legacyCaseReference, { exact: false }),
      super.expectText(paragraphs.firstResponse1v2DS),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
