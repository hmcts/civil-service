import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import { radioButtons } from './single-response-2v1-content.ts';

@AllMethodsStep()
export default class SingleResponse2v1Page extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectText(radioButtons.singleResponse.label),
      super.expectRadioYesLabel(radioButtons.singleResponse.yes.selector),
      super.expectRadioNoLabel(radioButtons.singleResponse.no.selector),
    ]);
  }

  async selectYes() {
    await super.clickBySelector(radioButtons.singleResponse.yes.selector);
  }

  async selectNo() {
    await super.clickBySelector(radioButtons.singleResponse.no.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
