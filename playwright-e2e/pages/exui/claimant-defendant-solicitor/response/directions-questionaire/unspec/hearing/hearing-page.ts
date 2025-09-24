import { Page } from '@playwright/test';
import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import { buttons, inputs, radioButtons, subheadings } from './hearing-content.ts';
import { Party } from '../../../../../../../models/partys.ts';
import DateFragment from '../../../../../fragments/date/date-fragment.ts';
import DateHelper from '../../../../../../../helpers/date-helper.ts';
import StringHelper from '../../../../../../../helpers/string-helper.ts';

@AllMethodsStep()
export default class HearingPage extends ExuiPage(BasePage) {
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
    this.dateFragment = dateFragment;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications(
      [
        super.verifyHeadings(ccdCaseData),
        super.expectSubheading(subheadings.availability, { count: 1 }),
        super.expectText(radioButtons.unavailableDateRequired.label, { count: 1 }),
        super.expectRadioYesLabel(
          radioButtons.unavailableDateRequired.yes.selector(this.claimantDefendantParty),
        ),
        super.expectRadioNoLabel(
          radioButtons.unavailableDateRequired.no.selector(this.claimantDefendantParty),
        ),
      ],
      { axePageInsertName: StringHelper.capitalise(this.solicitorParty.key) },
    );
  }

  async selectYesAvailabilityRequired() {
    await super.clickBySelector(
      radioButtons.unavailableDateRequired.yes.selector(this.claimantDefendantParty),
    );
  }

  async selectNoAvailabilityRequired() {
    await super.clickBySelector(
      radioButtons.unavailableDateRequired.no.selector(this.claimantDefendantParty),
    );
  }

  async addNewUnavailableDate() {
    await super.clickBySelector(buttons.addNewAvailability.selector(this.claimantDefendantParty));
    await super.expectSubheading(subheadings.unavailableDate, { count: 1 });
  }

  async selectSingleDate() {
    await super.clickBySelector(
      radioButtons.unavailableDateType.single.selector(this.claimantDefendantParty, 1),
    );
    const unavailableDate = DateHelper.addToToday({ months: 6 });
    await this.dateFragment.enterDate(unavailableDate, inputs.singleDate.selectorKey);
  }

  async selectDateRange() {
    await super.clickBySelector(
      radioButtons.unavailableDateType.range.selector(this.claimantDefendantParty, 1),
    );
    const unavailableDateFrom = DateHelper.addToToday({ months: 6 });
    const unavailableDateTo = DateHelper.addToToday({ months: 7 });
    await this.dateFragment.enterDate(unavailableDateFrom, inputs.dateFrom.selectorKey);
    await this.dateFragment.enterDate(unavailableDateTo, inputs.dateTo.selectorKey);
  }

  async submit() {
    await super.retryClickSubmit(() =>
      super.expectNoSubheading(subheadings.availability, { all: true, timeout: 500 }),
    );
  }
}
