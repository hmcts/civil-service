import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd-case-data.ts';
import ExuiPage from '../../../../../mixin-pages/exui-page/exui-page.ts';
import { confirmationHeading, subheadings } from './confirm-defendant-response-spec-content.ts';

@AllMethodsStep()
export default class ConfirmDefendantResponseSpecFullAdmitPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectHeading(confirmationHeading, {exact: false}),
      super.expectSubheading(subheadings.whatToDo),
      super.expectText(ccdCaseData.legacyCaseReference!, { exact: false }),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
