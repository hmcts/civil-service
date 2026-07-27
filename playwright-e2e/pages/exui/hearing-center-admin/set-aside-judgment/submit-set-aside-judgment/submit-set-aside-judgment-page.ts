import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import { subheading } from './submit-set-aside-judgment-content';

@AllMethodsStep()
export default class SubmitSetAsideJudgmentPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectSubheading(subheading),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
