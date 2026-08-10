import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../../models/ccd-case-data';
import ExuiPage from '../../../../mixin-pages/exui-page/exui-page';

@AllMethodsStep()
export default class SubmitCreateCaseFlagsPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications(super.verifyHeadings(ccdCaseData));
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
