import BasePage from '../../../../../../base/base-page.ts';
import ExuiPage from '../../../../exui-page/exui-page.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import { confirmationHeading, paragraphs } from './confirm-create-claim-spec-content.ts';

@AllMethodsStep()
export default class ConfirmCreateClaimSpecPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.expectHeading(confirmationHeading.part1),
      super.expectHeading(confirmationHeading.part2),
      super.expectText(paragraphs.descriptionText, { exact: false }),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
