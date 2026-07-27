import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import { confirmationHeadings } from './confirm-stay-case-content';

@AllMethodsStep()
export default class ConfirmStayCasePage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectHeading(confirmationHeadings.stayAdded),
      super.expectSubheading(confirmationHeadings.partiesNotified),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
