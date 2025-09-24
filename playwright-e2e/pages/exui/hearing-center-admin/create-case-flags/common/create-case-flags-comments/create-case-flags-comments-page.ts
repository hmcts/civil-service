import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../../models/ccd/ccd-case-data';
import ExuiPage from '../../../../exui-page/exui-page';
import { inputs } from './create-case-flags-comments-content';

@AllMethodsStep()
export default class CreateCaseFlagsCommentsPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectLabel(inputs.comments.label, { exact: false }),
    ]);
  }

  async addCaseFlagComment(comment: string) {
    await super.inputText(comment, inputs.comments.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
