import BasePage from '../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import ExuiPage from '../../../../exui-page/exui-page';
import { radioButtons } from './add-another-defendant-content.ts';

@AllMethodsStep()
export default class AddAnotherDefendantPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.expectLegend(radioButtons.anotherDefendant.label),
      super.expectRadioYesLabel(radioButtons.anotherDefendant.yes.selector),
      super.expectRadioNoLabel(radioButtons.anotherDefendant.no.selector),
    ]);
  }

  async selectYes() {
    await super.clickBySelector(radioButtons.anotherDefendant.yes.selector);
  }

  async selectNo() {
    await super.clickBySelector(radioButtons.anotherDefendant.no.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
