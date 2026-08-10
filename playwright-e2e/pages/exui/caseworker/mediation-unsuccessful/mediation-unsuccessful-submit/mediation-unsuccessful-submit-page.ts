import { AllMethodsStep } from '../../../../../decorators/test-steps';
import BasePage from '../../../../../base/base-page';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import CCDCaseData from '../../../../../models/ccd-case-data';

@AllMethodsStep()
export default class MediationUnsuccessfulSubmitPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
