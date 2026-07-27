import BasePage from '../../../../../base/base-page';
import ExuiHearingsPage from '../../../mixin-pages/exui-hearings-page/exui-hearings-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import { heading, radioButtons } from './hearing-stage-content';

@AllMethodsStep()
export default class HearingStagePage extends ExuiHearingsPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyCaseName(ccdCaseData),
      super.expectHeading(heading),
    ]);
  }

  async selectStage() {
    await super.clickBySelector(radioButtons.disposalHearing.selector);
  }

  async continue() {
    await super.clickContinue();
  }
}
