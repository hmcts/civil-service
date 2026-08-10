import { Page } from '@playwright/test';
import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import DateHelper from '../../../../../helpers/date-helper';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import DateFragment from '../../../fragments/date/date-fragment';
import { headings, inputs, radioButtons } from './set-aside-order-type-content';
import { getFormattedCaseId } from '../../../mixin-pages/exui-page/exui-content';

@AllMethodsStep()
export default class SetAsideOrderTypePage extends ExuiPage(BasePage) {
  private dateFragment: DateFragment;

  constructor(page: Page, dateFragment: DateFragment) {
    super(page);
    this.dateFragment = dateFragment;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.expectText(headings.setAside),
      super.expectText(headings.judgmentDetails),
      super.expectHeading(getFormattedCaseId(ccdCaseData.id!), { exact: false }),
      super.expectHeading(ccdCaseData.caseNamePublic!, { exact: false }),
      super.expectLegend(inputs.orderDate.label),
      super.expectLabel(radioButtons.orderAfterApplication.label),
      super.expectLabel(radioButtons.orderAfterDefence.label),
    ]);
  }

  async fillOrderFollowingApplication() {
    const yesterday = DateHelper.subtractFromToday({ days: 1, months: 1 });
    await this.dateFragment.enterDate(yesterday, inputs.orderDate.selectorKey);
    await super.clickBySelector(radioButtons.orderAfterApplication.selector);
    await super.inputTextByLabel(DateHelper.getTwoDigitDay(yesterday), 'Day', { index: 1 });
    await super.inputTextByLabel(DateHelper.getTwoDigitMonth(yesterday), 'Month', { index: 1 });
    await super.inputTextByLabel(yesterday.getFullYear(), 'Year', { index: 1 });
  }

  async fillOrderFollowingDefenceReceived() {
    const yesterday = DateHelper.subtractFromToday({ days: 1, months: 1 });
    await super.clickBySelector(radioButtons.orderAfterDefence.selector);
    await this.dateFragment.enterDate(yesterday, inputs.orderDate.selectorKey);
    await this.dateFragment.enterDate(yesterday, inputs.defenceReceivedDate.selectorKey);
  }

  async submit() {
    await super.retryClickSubmit(() =>
      super.expectNoSelector(radioButtons.orderAfterApplication.selector, { timeout: 3000 }),
    );
  }
}
