import BasePage from '../../../../../base/base-page.ts';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page.ts';
import { AllMethodsStep } from '../../../../../decorators/test-steps.ts';
import { headings } from './hearing-scheduled-submit-content.ts';

@AllMethodsStep()
export default class HearingScheduledSubmitPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([super.expectSubheading(headings.checkYourAnswers)]);
  }
  async submit() {
    await super.retryClickSubmit();
  }
}
