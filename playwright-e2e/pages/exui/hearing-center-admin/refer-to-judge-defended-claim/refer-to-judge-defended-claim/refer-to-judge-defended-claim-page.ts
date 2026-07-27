import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import { radioButtons } from './refer-to-judge-defended-claim-content';

@AllMethodsStep()
export default class ReferJudgeDefenceReceivedPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectLabel(radioButtons.confirm.label),
    ]);
  }

  async selectConfirm() {
    await super.clickBySelector(radioButtons.confirm.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
