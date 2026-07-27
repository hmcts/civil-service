import BasePage from '../../../../../base/base-page.ts';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page.ts';
import { AllMethodsStep } from '../../../../../decorators/test-steps.ts';
import { headings } from './evidence-upload-applicant-submit-content.ts';

@AllMethodsStep()
export default class EvidenceUploadSubmitPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([super.expectSubheading(headings.checkYourAnswers)]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
