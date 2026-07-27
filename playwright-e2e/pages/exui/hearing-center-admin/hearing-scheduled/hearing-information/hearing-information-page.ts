import BasePage from '../../../../../base/base-page.ts';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page.ts';
import { AllMethodsStep } from '../../../../../decorators/test-steps.ts';
import { heading, inputs } from './hearing-information-content.ts';

@AllMethodsStep()
export default class HearingInformationPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.expectHeading(heading),
      super.expectLabel(inputs.information.label)
    ]);
  }
  async enterMoreInformation() {
    await super.inputText('More information about the hearing', inputs.information.selector);
  }
  async submit() {
    await super.retryClickSubmit();
  }
}
