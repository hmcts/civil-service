import BasePage from '../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import ExuiPage from '../../../../mixin-pages/exui-page/exui-page';
import { labels, subheadings } from './pba-number-content.ts';

@AllMethodsStep()
export default class PbaNumberPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.expectText(subheadings.claimFee),
      super.expectText(labels.amountToPay, {count: 1}),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
