import BasePage from '../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import DateHelper from '../../../../../../helpers/date-helper.ts';
import ExuiPage from '../../../../exui-page/exui-page.ts';
import { inputs, subheadings } from './payment-set-date-content.ts';
import DateFragment from '../../../../fragments/date/date-fragment';
import CCDCaseData from '../../../../../../models/ccd/ccd-case-data.ts';
import partys from '../../../../../../constants/partys.ts';
import CaseDataHelper from '../../../../../../helpers/case-data-helper.ts';
import { ClaimantDefendantPartyType } from '../../../../../../models/claimant-defendant-party-types.ts';
import { Page } from '@playwright/test';

@AllMethodsStep()
export default class PaymentSetDatePage extends ExuiPage(BasePage) {
  private dateFragment: DateFragment;

  constructor(page: Page, dateFragment: DateFragment) {
    super(page);
    this.dateFragment = dateFragment;
  }

  async verifyContent(ccdCaseData: CCDCaseData, defendant1PartyType: ClaimantDefendantPartyType) {
    const defendantData = CaseDataHelper.buildClaimantAndDefendantData(
      partys.DEFENDANT_1,
      defendant1PartyType,
    );
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectSubheading(subheadings.paymentSetDate(defendantData.partyName)),
    ]);
  }

  async setPaymentDate() {
    const setDate = DateHelper.addToToday({ months: 1 });
    await this.dateFragment.enterDate(setDate, inputs.paymentSetDate.selectorKey);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
