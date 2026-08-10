import BasePage from '../../../../../base/base-page.ts';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page.ts';
import { AllMethodsStep } from '../../../../../decorators/test-steps.ts';
import { confirmationHeadings } from './hearing-scheduled-confirm-content.ts';

@AllMethodsStep()
export default class HearingScheduledConfirmPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.expectHeading(confirmationHeadings.confirmationHeading1),
      super.expectHeading(confirmationHeadings.confirmationHeading2),
    ]);
  }
  async submit() {
    await super.retryClickSubmit();
  }
}
