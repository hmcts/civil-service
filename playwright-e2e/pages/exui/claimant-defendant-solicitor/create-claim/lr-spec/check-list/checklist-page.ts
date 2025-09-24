import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ExuiPage from '../../../../exui-page/exui-page';
import { paragraphs, subheadings } from './checklist-content';

@AllMethodsStep()
export default class ChecklistPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.verifyHeadings(),
      super.expectSubheading(subheadings.lrSpec),
      super.expectText(paragraphs.paragraph1),
      super.expectText(paragraphs.paragraph2),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
