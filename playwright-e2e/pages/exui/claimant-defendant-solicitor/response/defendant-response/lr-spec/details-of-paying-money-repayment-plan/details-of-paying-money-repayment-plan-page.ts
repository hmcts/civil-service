import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd-case-data.ts';
import ExuiPage from '../../../../../mixin-pages/exui-page/exui-page.ts';
import { heading, radioButtons } from './details-of-paying-money-repayment-plan-content.ts';

@AllMethodsStep()
export default class DetailsOfPayingMoneyRepaymentPlanPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectSubheading(heading, { exact: false }),
    ]);
  }

  async selectNo() {
    await super.clickBySelector(radioButtons.courtOrderPayment.no.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
