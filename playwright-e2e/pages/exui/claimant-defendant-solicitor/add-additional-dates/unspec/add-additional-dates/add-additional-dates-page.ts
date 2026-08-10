import { Page } from '@playwright/test';
import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import DateHelper from '../../../../../../helpers/date-helper';
import ExuiPage from '../../../../mixin-pages/exui-page/exui-page';
import CCDCaseData from '../../../../../../models/ccd-case-data';
import DateFragment from '../../../../fragments/date/date-fragment';
import { subheadings, buttons, inputs, radioButtons } from './add-additional-dates-content';

@AllMethodsStep()
export default class AddAdditionalDatesPage extends ExuiPage(BasePage) {
  private dateFragment: DateFragment;

  constructor(page: Page, dateFragment: DateFragment) {
    super(page);
    this.dateFragment = dateFragment;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectSubheading(subheadings.unavailableDates),
      super.expectButton(buttons.addNew.label),
    ]);
  }

  async addSingleDateAndDateRange() {
    await super.clickBySelector(buttons.addNew.selector);
    await super.expectSubheading(subheadings.unavailableDates, { headingLevel: 3 });
    await super.clickBySelector(radioButtons.unavailableDateType.single.selector);
    await this.dateFragment.enterDate(
      DateHelper.addToToday({ days: 15 }),
      inputs.singleDate.selectorKey,
      { containerSelector: inputs.singleDate.containerSelector },
    );

    await super.clickBySelector(buttons.addNew.selector);
    await super.expectSubheading(subheadings.unavailableDates2, { headingLevel: 3 });
    await super.clickBySelector(radioButtons.unavailableDateType.range.selector);
    await super.clickBySelector(radioButtons.unavailableDateType.range.selector); // needs clicking twice sometimes
    await this.dateFragment.enterDate(
      DateHelper.addToToday({ days: 30 }),
      inputs.dateFrom.selectorKey,
      { containerSelector: inputs.dateFrom.containerSelector },
    );
    await this.dateFragment.enterDate(
      DateHelper.addToToday({ days: 35 }),
      inputs.dateTo.selectorKey,
      { containerSelector: inputs.dateTo.containerSelector },
    );
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
