import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import { subheading, radioButtons } from './final-order-select-content';

@AllMethodsStep()
export default class FinalOrderSelectPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectSubheading(subheading, { headingLevel: 3 }),
      super.expectLabel(radioButtons.freeFormOrder.label),
    ]);
  }

  async selectFreeFormOrder() {
    await super.clickBySelector(radioButtons.freeFormOrder.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
