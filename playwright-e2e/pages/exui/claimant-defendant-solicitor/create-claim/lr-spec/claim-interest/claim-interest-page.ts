import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ExuiPage from '../../../../exui-page/exui-page';
import { radioButtons, subheadings } from './claim-interest-content';

@AllMethodsStep()
export default class ClaimInterestPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.expectText(subheadings.claimInterest),
      super.expectLegend(radioButtons.claimInterest.label),
      super.expectRadioYesLabel(radioButtons.claimInterest.yes.selector),
      super.expectRadioNoLabel(radioButtons.claimInterest.no.selector),
    ]);
  }

  async selectYes() {
    await super.clickBySelector(radioButtons.claimInterest.yes.selector);
  }

  async selectNo() {
    await super.clickBySelector(radioButtons.claimInterest.no.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
