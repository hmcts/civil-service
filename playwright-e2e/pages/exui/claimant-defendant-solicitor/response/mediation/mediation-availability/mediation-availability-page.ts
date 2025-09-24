import { Page } from '@playwright/test';
import { Party } from '../../../../../../models/partys.ts';
import BasePage from '../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../../exui-page/exui-page.ts';
import { subheadings, radioButtons, buttons, inputs } from './mediation-availability-content.ts';
import DateHelper from '../../../../../../helpers/date-helper.ts';
import DateFragment from '../../../../fragments/date/date-fragment.ts';
import StringHelper from '../../../../../../helpers/string-helper.ts';

@AllMethodsStep()
export default class MediationAvailabilityPage extends ExuiPage(BasePage) {
  private dateFragment: DateFragment;
  private claimantDefendantParty: Party;
  private solicitorParty: Party;

  constructor(
    page: Page,
    dateFragment: DateFragment,
    claimantDefendantParty: Party,
    solicitorParty: Party,
  ) {
    super(page);
    this.dateFragment = dateFragment;
    this.claimantDefendantParty = claimantDefendantParty;
    this.solicitorParty = solicitorParty;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications(
      [
        super.verifyHeadings(ccdCaseData),
        super.expectSubheading(subheadings.mediationAvailability, { count: 1 }),
        super.expectLegend(radioButtons.mediationAvailability.label, { count: 1 }),
        super.expectText(radioButtons.mediationAvailability.hintText, { count: 1 }),
      ],
      { axePageInsertName: StringHelper.capitalise(this.solicitorParty.key) },
    );
  }

  async selectYes() {
    await super.clickBySelector(
      radioButtons.mediationAvailability.yes.selector(this.claimantDefendantParty),
    );
  }

  async selectNo() {
    await super.clickBySelector(
      radioButtons.mediationAvailability.no.selector(this.claimantDefendantParty),
    );
  }

  async addNewUnavailableDate() {
    await super.clickBySelector(buttons.addNew.selector(this.claimantDefendantParty));
    // await super.expectSubheading(subheadings.unavailableDates);
  }

  async selectSingleDate() {
    await super.clickBySelector(
      radioButtons.unavailableDateType.single.selector(this.claimantDefendantParty, 1),
    );
    const unavailableDate = DateHelper.addToToday({ months: 1 });
    await this.dateFragment.enterDate(unavailableDate, inputs.singleDate.selectorKey);
  }

  async selectDateRange(unavailableDateNumber: number) {
    await super.clickBySelector(
      radioButtons.unavailableDateType.range.selector(
        this.claimantDefendantParty,
        unavailableDateNumber,
      ),
    );
    const unavailableDateFrom = DateHelper.addToToday({ months: 1 });
    const unavailableDateTo = DateHelper.addToToday({ months: 2 });
    await this.dateFragment.enterDate(unavailableDateFrom, inputs.dateFrom.selectorKey);
    await this.dateFragment.enterDate(unavailableDateTo, inputs.dateTo.selectorKey);
  }

  async submit() {
    await super.retryClickSubmit(() =>
      this.expectNoSelector(
        radioButtons.mediationAvailability.yes.selector(this.claimantDefendantParty),
        { timeout: 500 },
      ),
    );
  }
}
