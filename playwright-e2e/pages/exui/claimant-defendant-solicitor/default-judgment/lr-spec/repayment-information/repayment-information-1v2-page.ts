import BasePage from '../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import DateHelper from '../../../../../../helpers/date-helper.ts';
import ExuiPage from '../../../../exui-page/exui-page.ts';
import { subheadings, inputs, radioButtons } from './repayment-information-content.ts';
import DateFragment from '../../../../fragments/date/date-fragment';
import CCDCaseData from '../../../../../../models/ccd/ccd-case-data.ts';
import { Page } from '@playwright/test';

@AllMethodsStep()
export default class RepaymentInformation1v2Page extends ExuiPage(BasePage) {
  private dateFragment: DateFragment;

  constructor(page: Page, dateFragment: DateFragment) {
    super(page);
    this.dateFragment = dateFragment;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectSubheading(subheadings.instalments1v2),
      super.expectLabel(inputs.regularPayments.label),
      super.expectLegend(radioButtons.howOften.label),
      super.expectLabel(radioButtons.howOften.every2Weeks.label),
      super.expectLabel(radioButtons.howOften.everyMonth.label),
      super.expectLabel(radioButtons.howOften.everyWeek.label),
    ]);
  }

  async regularPaymentsAmount() {
    await super.inputText(20, inputs.regularPayments.selector);
  }

  async selectEveryWeek() {
    await super.clickBySelector(radioButtons.howOften.everyWeek.selector);
  }

  async selectEvery2Weeks() {
    await super.clickBySelector(radioButtons.howOften.every2Weeks.selector);
  }

  async selectEveryMonth() {
    await super.clickBySelector(radioButtons.howOften.everyMonth.selector);
  }

  async firstInstalmentDate() {
    const setDate = DateHelper.addToToday({ months: 2 });
    await this.dateFragment.enterDate(setDate, inputs.firstInstalmentDate.selectorKey);
  }

  async submit() {
    await super.retryClickSubmit(() =>
      super.expectNoLabel(inputs.regularPayments.label, { timeout: 500 }),
    );
  }
}
