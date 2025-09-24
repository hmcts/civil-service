import BasePage from '../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import DateHelper from '../../../../../../helpers/date-helper.ts';
import ExuiPage from '../../../../exui-page/exui-page.ts';
import { inputs, radioButtons, subheadings } from './repayment-information-content.ts';
import DateFragment from '../../../../fragments/date/date-fragment';
import CCDCaseData from '../../../../../../models/ccd/ccd-case-data.ts';
import CaseDataHelper from '../../../../../../helpers/case-data-helper.ts';
import partys from '../../../../../../constants/partys.ts';
import { ClaimantDefendantPartyType } from '../../../../../../models/claimant-defendant-party-types.ts';
import { Page } from '@playwright/test';

@AllMethodsStep()
export default class RepaymentInformationPage extends ExuiPage(BasePage) {
  private dateFragment: DateFragment;

  constructor(page: Page, dateFragment: DateFragment) {
    super(page);
    this.dateFragment = dateFragment;
  }

  async verifyContent(ccdCaseData: CCDCaseData, defendantPartyType: ClaimantDefendantPartyType) {
    const defendantData = CaseDataHelper.buildClaimantAndDefendantData(
      partys.DEFENDANT_1,
      defendantPartyType,
    );
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectSubheading(subheadings.instalments(defendantData.partyName)),
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

  async selectWeeklyRepayments() {
    await super.clickBySelector(radioButtons.howOften.everyWeek.selector);
  }

  async selectBiWeeklyRepayments() {
    await super.clickBySelector(radioButtons.howOften.every2Weeks.selector);
  }

  async selectMonthlyRepayments() {
    await super.clickBySelector(radioButtons.howOften.everyMonth.selector);
  }

  async firstInstalmentDate() {
    const setDate = DateHelper.addToToday({ months: 1 });
    await this.dateFragment.enterDate(setDate, inputs.firstInstalmentDate.selectorKey);
  }

  async submit() {
    await super.retryClickSubmit(() =>
      super.expectNoLabel(inputs.regularPayments.label, { timeout: 500 }),
    );
  }
}
