import BasePage from '../../../../../base/base-page';
import ExuiHearingsPage from '../../../mixin-pages/exui-hearings-page/exui-hearings-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import { heading, links } from './hearing-confirm-content';

@AllMethodsStep()
export default class HearingConfirmPage extends ExuiHearingsPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([super.expectHeading(heading)]);
  }

  async clickViewStatus() {
    await super.clickLink(links.viewStatusInHearingsTab);
  }

  async continue() {
    throw new Error('Method not implemented.');
  }
}
