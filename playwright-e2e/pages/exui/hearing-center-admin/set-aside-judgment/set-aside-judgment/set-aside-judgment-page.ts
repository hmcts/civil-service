import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import { headings, inputs, subheading, radioButtons } from './set-aside-judgment-content';
import { getFormattedCaseId } from '../../../mixin-pages/exui-page/exui-content';

@AllMethodsStep()
export default class SetAsideJudgmentPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.expectText(headings.setAside),
      super.expectText(headings.judgmentDetails),
      super.expectHeading(getFormattedCaseId(ccdCaseData.id!), { exact: false }),
      super.expectHeading(ccdCaseData.caseNamePublic!, { exact: false }),

      super.expectText(subheading),
      super.expectLabel(radioButtons.judgeOrder.label),
      super.expectLabel(radioButtons.judgmentError.label),
    ]);
  }

  async selectJudgeOrder() {
    await super.clickBySelector(radioButtons.judgeOrder.selector);
  }

  async selectJudgmentError() {
    await super.clickBySelector(radioButtons.judgmentError.selector);
    await super.inputText(inputs.judgmentErrorText.text, inputs.judgmentErrorText.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
