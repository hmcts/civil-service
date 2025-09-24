import { Page } from '@playwright/test';
import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import { radioButtons, inputs } from './defence-route-content.ts';
import DateHelper from '../../../../../../../helpers/date-helper.ts';
import { Party } from '../../../../../../../models/partys.ts';
import StringHelper from '../../../../../../../helpers/string-helper.ts';
import DateFragment from '../../../../../fragments/date/date-fragment.ts';

@AllMethodsStep()
export default class DefenceRoutePage extends ExuiPage(BasePage) {
  private dateFragment: DateFragment;
  private defendantParty: Party;
  private solicitorParty: Party;

  constructor(
    page: Page,
    dateFragment: DateFragment,
    defendantParty: Party,
    solicitorParty: Party,
  ) {
    super(page);
    this.dateFragment = dateFragment;
    this.defendantParty = defendantParty;
    this.solicitorParty = solicitorParty;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications(
      [
        super.verifyHeadings(ccdCaseData),
        super.expectLabel(radioButtons.defenceRoute.hasPaid.label, { count: 1 }),
        super.expectLabel(radioButtons.defenceRoute.disputesClaim.label, { count: 1 }),
      ],
      { axePageInsertName: StringHelper.capitalise(this.solicitorParty.key) },
    );
  }

  async selectHasPaid() {
    await super.clickBySelector(radioButtons.defenceRoute.hasPaid.selector(this.defendantParty));
  }

  async selectDisputesClaim() {
    await super.clickBySelector(
      radioButtons.defenceRoute.disputesClaim.selector(this.defendantParty),
    );
  }

  async fillInHasPaid() {
    const datePaid = DateHelper.subtractFromToday({ months: 1 });
    await super.inputText('500', inputs.amountPaid.selector(this.defendantParty));
    await this.dateFragment.enterDate(datePaid, inputs.amountPaidDate.selectorKey);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
