import BasePage from '../../../../../base/base-page.ts';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page.ts';
import { AllMethodsStep } from '../../../../../decorators/test-steps.ts';
import { heading, radioButtons } from './hearing-notice-select-content.ts';

@AllMethodsStep()
export default class HearingNoticeSelectPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.expectHeading(heading),
      super.expectLabel(radioButtons.smallClaims.label),
      super.expectLabel(radioButtons.trial.label),
      super.expectLabel(radioButtons.other.label),
    ]);
  }
  async selectSmallClaims() {
    await super.clickBySelector(radioButtons.smallClaims.selector);
  }

  async selectTrail() {
    await super.clickBySelector(radioButtons.smallClaims.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
