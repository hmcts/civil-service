import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd-case-data.ts';
import ExuiPage from '../../../../../mixin-pages/exui-page/exui-page.ts';
import { radioButtons } from './defendant-debts-content.ts';

@AllMethodsStep()
export default class DefendantDebtsPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectLegend(radioButtons.loanCredit.label),
      super.expectLabel(radioButtons.loanCredit.yes.label),
      super.expectLabel(radioButtons.loanCredit.no.label),
    ]);
  }

  async selectNo() {
    await super.clickBySelector(radioButtons.loanCredit.no.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
