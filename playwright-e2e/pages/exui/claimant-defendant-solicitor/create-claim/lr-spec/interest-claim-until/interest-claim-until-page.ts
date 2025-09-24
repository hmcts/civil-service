import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ExuiPage from '../../../../exui-page/exui-page';
import { radioButtons, subheadings } from './interest-claim-until-content';

@AllMethodsStep()
export default class InterestClaimUntilPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([super.expectText(subheadings.interestClaimUntil)]);
  }

  async selectSameInterest() {
    await super.clickBySelector(radioButtons.interestClaimUntil.submitDate.selector);
  }

  async selectBreakDownInterest() {
    await super.clickBySelector(radioButtons.interestClaimUntil.untilSettledOrJudgement.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
