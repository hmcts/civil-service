import BasePage from '../../../../../base/base-page';
import ExuiHearingsPage from '../../../mixin-pages/exui-hearings-page/exui-hearings-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import { heading, checkboxes } from './hearing-cancel-content';

@AllMethodsStep()
export default class HearingCancelPage extends ExuiHearingsPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([super.expectText(heading)]);
  }

  async selectListedInError() {
    await super.clickBySelector(checkboxes.listedInError.selector);
  }

  async continue() {
    await super.clickContinue();
  }
}
