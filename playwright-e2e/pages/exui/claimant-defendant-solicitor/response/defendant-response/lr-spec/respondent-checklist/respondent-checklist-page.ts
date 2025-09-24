import BasePage from '../../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps';
import ExuiPage from '../../../../../exui-page/exui-page';
import { paragraphs } from './respondent-checklist-content';

@AllMethodsStep()
export default class RespondentChecklistPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.expectText(paragraphs.descriptionText1),
      super.expectText(paragraphs.descriptionText2),
      super.expectText(paragraphs.descriptionText3),
      super.expectText(paragraphs.descriptionText4),
      super.expectText(paragraphs.descriptionText5),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
