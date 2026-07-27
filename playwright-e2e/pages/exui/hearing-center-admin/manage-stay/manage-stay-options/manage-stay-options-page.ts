import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import { heading, radioButtons } from './manage-stay-options-content';

@AllMethodsStep()
export default class ManageStayOptionsPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectHeading(heading),
      super.expectText(radioButtons.label),
      super.expectLabel(radioButtons.requestUpdate.label),
      super.expectLabel(radioButtons.liftStay.label),
    ]);
  }

  async selectRequestUpdate() {
    await super.clickBySelector(radioButtons.requestUpdate.selector);
  }

  async selectLiftStay() {
    await super.clickBySelector(radioButtons.liftStay.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
