import { Page } from '@playwright/test';
import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import { buttons, radioButtons, subheadings, inputs } from './small-claim-hearing-content.ts';
import { Party } from '../../../../../../../models/partys.ts';
import DateFragment from '../../../../../fragments/date/date-fragment.ts';
import DateHelper from '../../../../../../../helpers/date-helper.ts';
import StringHelper from '../../../../../../../helpers/string-helper.ts';

@AllMethodsStep()
export default class SmallClaimHearingPage extends ExuiPage(BasePage) {
  private dateFragment: DateFragment;
  private defendantParty: Party;
  private solicitorParty: Party;

  constructor(page: Page, dateFrament: DateFragment, defendantParty: Party, solicitorParty: Party) {
    super(page);
    this.dateFragment = dateFrament;
    this.defendantParty = defendantParty;
    this.solicitorParty = solicitorParty;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications(
      [
        super.verifyHeadings(ccdCaseData),
        super.expectSubheading(subheadings.availability, { count: 1 }),
        super.expectText(radioButtons.unavailableDatesRequired.label, { count: 1 }),
      ],
      { axePageInsertName: StringHelper.capitalise(this.solicitorParty.key) },
    );
  }

  async selectYesAvailabilityRequired() {
    await super.clickBySelector(
      radioButtons.unavailableDatesRequired.yes.selector(this.defendantParty),
    );
  }

  async selectNoAvailabilityRequired() {
    await super.clickBySelector(
      radioButtons.unavailableDatesRequired.no.selector(this.defendantParty),
    );
  }

  async selectYesInterpreter() {
    const selector = radioButtons.interpreter.yes.selector(this.defendantParty);
    await super.retryClickBySelector(selector, () =>
      super.expectOptionChecked(selector, { timeout: 500 }),
    );
  }

  async selectNoInterpreter() {
    await super.clickBySelector(radioButtons.interpreter.no.selector(this.defendantParty));
  }

  async enterTypeOfInterpreter() {
    await super.inputText('English', inputs.interpreterType.selector(this.defendantParty));
  }

  async addNewUnavailableDate() {
    await super.clickBySelector(buttons.addNewAvailability.selector(this.defendantParty));
  }

  async selectSingleDate() {
    await super.clickBySelector(
      radioButtons.availabilityOptions.single.selector(this.defendantParty, 1),
    );
    const unavailableDate = DateHelper.addToToday({ months: 6 });
    await this.dateFragment.enterDate(unavailableDate, inputs.singleDate.selectorKey);
  }

  async selectDateRange() {
    await super.clickBySelector(
      radioButtons.availabilityOptions.range.selector(this.defendantParty, 1),
    );
    const unavailableDateFrom = DateHelper.addToToday({ months: 6 });
    const unavailableDateTo = DateHelper.addToToday({ months: 7 });
    await this.dateFragment.enterDate(unavailableDateFrom, inputs.dateFrom.selectorKey);
    await this.dateFragment.enterDate(unavailableDateTo, inputs.dateTo.selectorKey);
  }

  async submit() {
    await super.retryClickSubmit(() =>
      super.expectNoSelector(
        radioButtons.unavailableDatesRequired.yes.selector(this.defendantParty),
        { timeout: 500 },
      ),
    );
  }
}
