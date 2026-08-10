import BasePage from '../../../../../base/base-page.ts';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page.ts';
import { AllMethodsStep } from '../../../../../decorators/test-steps.ts';
import preferredCourts from '../../../../../config/preferred-courts.ts';
import partys from '../../../../../constants/users/partys.ts';
import DateHelper from '../../../../../helpers/date-helper.ts';
import { heading, radioButtons, inputs, dropdowns } from './hearing-details-content.ts';
import DateFragment from '../../../fragments/date/date-fragment.ts';
import { Page } from 'playwright-core';

@AllMethodsStep()
export default class HearingDetailsPage extends ExuiPage(BasePage) {
  private dateFragment: DateFragment;

  constructor(page: Page, dateFragment: DateFragment) {
    super(page);
    this.dateFragment = dateFragment;
  }

  async verifyContent() {
    await super.runVerifications([
      super.expectHeading(heading),
      super.expectText(dropdowns.venue.label),
      super.expectLabel(radioButtons.inPerson.label),
      super.expectLabel(radioButtons.video.label),
      super.expectLabel(radioButtons.telephone.label),
      super.expectLabel(dropdowns.startTime.label),
      super.expectLabel(dropdowns.duration.label, { count: 1 }),
    ]);
  }
  async enterHearingDetails() {
    const hearingDate = DateHelper.addToToday({ days: 2, workingDay: true });
    await super.selectFromDropdown(
      preferredCourts[partys.CLAIMANT_1.key].default!,
      dropdowns.venue.selector,
    );
    await super.clickBySelector(radioButtons.inPerson.selector);
    await this.dateFragment.enterDate(hearingDate, inputs.date.selectorKey);
    await super.selectFromDropdown(1, dropdowns.startTime.selector);
    await super.selectFromDropdown(1, dropdowns.duration.selector);
  }
  async submit() {
    await super.retryClickSubmit(() =>
      super.expectNoSelector(dropdowns.venue.selector, { timeout: 3000 }),
    );
  }
}
