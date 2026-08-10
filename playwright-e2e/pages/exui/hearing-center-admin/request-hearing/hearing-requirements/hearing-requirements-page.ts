import BasePage from '../../../../../base/base-page';
import ExuiHearingsPage from '../../../mixin-pages/exui-hearings-page/exui-hearings-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import { heading, paragraph } from './hearing-requirements-content';

@AllMethodsStep()
export default class HearingRequirementsPage extends ExuiHearingsPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyCaseName(ccdCaseData),
      super.expectHeading(heading),
      super.expectText(paragraph, { count: 2 }),
    ]);
  }

  async continue() {
    await super.clickContinue();
  }
}
