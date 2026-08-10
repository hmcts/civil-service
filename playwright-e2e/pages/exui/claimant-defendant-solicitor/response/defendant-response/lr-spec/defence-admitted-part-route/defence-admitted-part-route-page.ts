import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd-case-data.ts';
import ExuiPage from '../../../../../mixin-pages/exui-page/exui-page.ts';
import { radioButtons, inputs } from './defence-admitted-part-route-content.ts';

@AllMethodsStep()
export default class DefenceAdmittedPartRoutePage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([super.verifyHeadings(ccdCaseData)]);
  }

  async selectNo() {
    await super.clickBySelector(radioButtons.specDefenceAdmitted.no.selector);
  }

  async enterOwingAmount() {
    await super.inputText('100', inputs.owingAmount.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
