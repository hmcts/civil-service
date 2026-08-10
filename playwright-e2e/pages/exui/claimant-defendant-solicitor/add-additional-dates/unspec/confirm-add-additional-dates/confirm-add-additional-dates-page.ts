import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../../models/ccd-case-data';
import ExuiPage from '../../../../mixin-pages/exui-page/exui-page';
import { confirmationHeading } from './confirm-add-additional-dates-content';

@AllMethodsStep()
export default class ConfirmAddAdditionalDatesPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectHeading(confirmationHeading),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
