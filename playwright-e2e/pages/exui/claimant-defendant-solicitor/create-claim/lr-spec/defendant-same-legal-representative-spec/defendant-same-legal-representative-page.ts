import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import { subheadings, labels, selectors } from './defendant-same-legal-representative-content';
import ExuiPage from '../../../../exui-page/exui-page.ts';

@AllMethodsStep()
export default class DefendantSameLegalRepresentativePage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.verifyHeadings(),
      super.expectText(subheadings.secondDefendantSameLegalRep),
      super.expectText(labels.yes),
      super.expectText(labels.no, { ignoreDuplicates: true }),
    ]);
  }

  async clickYes() {
    await super.clickBySelector(selectors.respondent2SameLegalRepresentativeYes);
  }

  async clickNo() {
    await super.clickBySelector(selectors.respondent2SameLegalRepresentativeNo);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
