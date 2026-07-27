import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import { headings } from './submit-decision-on-reconsideration-request-content';

@AllMethodsStep()
export default class SubmitDecisionOnReconsiderationRequestPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([super.expectSubheading(headings.checkYourAnswers)]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
