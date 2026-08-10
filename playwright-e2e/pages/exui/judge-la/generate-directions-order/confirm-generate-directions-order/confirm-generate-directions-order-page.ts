import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import { subheadings } from './confirm-generate-directions-order-content';

@AllMethodsStep()
export default class ConfirmGenerateDirectionsOrderPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectSubheading(subheadings),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
