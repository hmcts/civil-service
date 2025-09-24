import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ExuiPage from '../../../../exui-page/exui-page';
import { radioButtons, subheadings } from './flight-delay-claim-content';

@AllMethodsStep()
export default class FlightDelayClaimPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.expectSubheading(subheadings.airlineClaim),
      super.expectRadioYesLabel(radioButtons.flightDelay.yes.selector),
      super.expectRadioNoLabel(radioButtons.flightDelay.no.selector),
    ]);
  }

  async selectYes() {
    await super.clickBySelector(radioButtons.flightDelay.yes.selector);
  }

  async selectNo() {
    await super.clickBySelector(radioButtons.flightDelay.no.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
