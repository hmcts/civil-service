import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd-case-data.ts';
import ExuiPage from '../../../../../mixin-pages/exui-page/exui-page.ts';
import { heading, inputs } from './why-does-not-pay-immediately-content.ts';

@AllMethodsStep()
export default class WhyDoesNotPayImmediatelyPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectSubheading(heading, { exact: false }),
    ]);
  }

  async enterReasons() {
    await super.inputText('reasons', inputs.reasons.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
