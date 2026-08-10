import BasePage from '../../../../../base/base-page.ts';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page.ts';
import { AllMethodsStep } from '../../../../../decorators/test-steps.ts';
import { heading, dropdowns, inputs } from './transfer-online-case-content.ts';
import preferredCourts from '../../../../../config/preferred-courts.ts';

@AllMethodsStep()
export default class TransferOnlineCasePage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.expectHeading(heading),
      super.expectLabel(dropdowns.courtLocation.label),
      super.expectLabel(inputs.reasonForTransfer.label),
    ]);
  }

  async selectCourt() {
    await super.selectFromDropdown(
      preferredCourts.transfer.default,
      dropdowns.courtLocation.selector,
    );
  }

  async enterReason() {
    await super.inputText('Transfer case online', inputs.reasonForTransfer.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
