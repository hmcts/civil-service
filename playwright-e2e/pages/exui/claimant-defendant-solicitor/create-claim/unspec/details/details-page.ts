import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ExuiPage from '../../../../exui-page/exui-page';
import { inputs, paragraphs, subheadings } from './details-content';

@AllMethodsStep()
export default class DetailsPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.verifyHeadings(),
      super.expectSubheading(subheadings.briefDetails),
      super.expectText(paragraphs.descriptionText1),
      super.expectText(paragraphs.descriptionText2),
      super.expectLabel(inputs.details.label),
    ]);
  }

  async enterDetails() {
    await super.inputText('This is the details of the claim', inputs.details.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
