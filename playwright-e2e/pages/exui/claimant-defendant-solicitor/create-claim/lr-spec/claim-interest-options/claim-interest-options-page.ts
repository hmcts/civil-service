import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ExuiPage from '../../../../exui-page/exui-page';
import { radioButtons, subheadings } from './claim-interest-options-content';

@AllMethodsStep()
export default class ClaimInterestOptionsPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([super.expectText(subheadings.claimInterestOptions)]);
  }

  async selectSameRateInterest() {
    await super.clickBySelector(radioButtons.claimInterestOptions.sameRateInterest.selector);
  }

  async selectBreakDownInterest() {
    await super.clickBySelector(radioButtons.claimInterestOptions.breakDownInterest.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
