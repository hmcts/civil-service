import BasePage from '../../../../../base/base-page';
import ExuiHearingsPage from '../../../mixin-pages/exui-hearings-page/exui-hearings-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import { heading, button } from './hearing-view-summary-content';

@AllMethodsStep()
export default class HearingViewSummaryPage extends ExuiHearingsPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([super.expectHeading(heading)]);
  }

  async editHearing() {
    await super.clickBySelector(button.editHearing.selector);
  }

  async continue() {
    throw new Error('Method not implemented.');
  }
}
