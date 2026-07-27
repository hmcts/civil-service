import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import { inputs, radioButtons, subheading } from './free-form-order-content';

@AllMethodsStep()
export default class FreeFormOrderPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectSubheading(subheading),
    ]);
  }

  async enterOrderDetails() {
    await super.inputText('The court records that...', inputs.recitals);
    await super.inputText('The court orders that...', inputs.order);
    await super.clickBySelector(radioButtons.orderOnCourtsListCourtInitiative.selector);
    await super.inputText('Hearing notes...', inputs.hearingNotes);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
