import { Page } from '@playwright/test';
import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import DateHelper from '../../../../../../../helpers/date-helper.ts';
import CCDCaseData from '../../../../../../../models/ccd-case-data.ts';
import ExuiPage from '../../../../../mixin-pages/exui-page/exui-page.ts';
import DateFragment from '../../../../../fragments/date/date-fragment.ts';
import { inputs, radioButtons } from './repayment-plan-content.ts';

@AllMethodsStep()
export default class RepaymentPlanPage extends ExuiPage(BasePage) {
  private dateFragment: DateFragment;

  constructor(page: Page, dateFragment: DateFragment) {
    super(page);
    this.dateFragment = dateFragment;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectLabel(inputs.paymentAmount.label),
      super.expectLegend(radioButtons.repaymentFrequency.label),
      super.expectLabel(radioButtons.repaymentFrequency.everyWeek.label),
      super.expectLabel(radioButtons.repaymentFrequency.everyTwoWeeks.label),
      super.expectLabel(radioButtons.repaymentFrequency.everyMonth.label),
      super.expectLegend(inputs.firstRepaymentDate.label),
    ]);
  }

  async enterPaymentAmount() {
    await super.inputText('10', inputs.paymentAmount.selector);
  }

  async selectMonthlyFrequency() {
    await super.clickBySelector(radioButtons.repaymentFrequency.everyMonth.selector);
  }

  async enterFirstRepaymentDate() {
    const date = DateHelper.addToToday({ months: 2 });
    await this.dateFragment.enterDate(date, inputs.firstRepaymentDate.selectorKey);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
