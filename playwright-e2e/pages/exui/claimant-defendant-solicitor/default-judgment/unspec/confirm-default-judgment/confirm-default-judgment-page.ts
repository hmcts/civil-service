import BasePage from '../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../../exui-page/exui-page.ts';
import { confirmationHeading } from './confirm-default-judgment-content.ts';

@AllMethodsStep()
export default class ConfirmDefaultJudgmentPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectHeading(confirmationHeading, { exact: false }),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
