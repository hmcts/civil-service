import BasePage from '../../../../../base/base-page';
import ExuiHearingsPage from '../../../mixin-pages/exui-hearings-page/exui-hearings-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import { heading } from './hearing-submit-content';

@AllMethodsStep()
export default class HearingSubmitPage extends ExuiHearingsPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyCaseName(ccdCaseData),
      super.expectHeading(heading),
    ]);
  }

  async continue() {
    await super.submitRequest();
  }
}
