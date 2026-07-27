import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import { subheading, inputs } from './request-for-reconsideration-content';

@AllMethodsStep()
export default class RequestForReconsiderationPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectSubheading(subheading),
      super.expectSelector(inputs.reason.selector),
    ]);
  }

  async enterReason() {
    await super.inputText('Testing Request for Reconsideration', inputs.reason.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
