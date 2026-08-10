import BasePage from '../../../../../base/base-page';
import ExuiHearingsPage from '../../../mixin-pages/exui-hearings-page/exui-hearings-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import { heading, radioButtons, checkboxes } from './hearing-judge-content';

@AllMethodsStep()
export default class JudgeTypesPage extends ExuiHearingsPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyCaseName(ccdCaseData),
      super.expectHeading(heading),
    ]);
  }

  async selectJudges() {
    await super.clickBySelector(radioButtons.wantSpecificJudge.no.selector);
    await super.expectText(checkboxes.judgeTypes.label);
    await super.clickByLabel(checkboxes.judgeTypes.districtJudge.label);
    await super.clickByLabel(checkboxes.judgeTypes.deputyDistrictJudgeFeePaid.label);
  }

  async updateJudges() {
    await super.clickByLabel(checkboxes.judgeTypes.districtJudge.label);
  }

  async continue() {
    await super.clickContinue();
  }
}
