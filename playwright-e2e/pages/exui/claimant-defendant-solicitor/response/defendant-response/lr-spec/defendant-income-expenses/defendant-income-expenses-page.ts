import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd-case-data.ts';
import ExuiPage from '../../../../../mixin-pages/exui-page/exui-page.ts';
import { radioButtons } from './defendant-income-expenses-content.ts';

@AllMethodsStep()
export default class DefendantIncomeExpensesPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectLegend(radioButtons.carerAllowance.label),
      super.expectLabel(radioButtons.carerAllowance.yes.label),
      super.expectLabel(radioButtons.carerAllowance.no.label),
    ]);
  }

  async selectNo() {
    await super.clickBySelector(radioButtons.carerAllowance.no.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
