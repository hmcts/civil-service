import BasePage from '../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import { radioButtons } from './legal-representation-respondent-2-content.ts';
import ExuiPage from '../../../../exui-page/exui-page.ts';

@AllMethodsStep()
export default class LegalRepresentationRespondent2Page extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.verifyHeadings(),
      super.expectLegend(radioButtons.defendantRepresented.label),
      super.expectRadioYesLabel(radioButtons.defendantRepresented.yes.selector),
      super.expectRadioNoLabel(radioButtons.defendantRepresented.no.selector),
    ]);
  }

  async selectYes() {
    await super.clickBySelector(radioButtons.defendantRepresented.yes.selector);
  }

  async clickNo() {
    await super.clickBySelector(radioButtons.defendantRepresented.no.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
