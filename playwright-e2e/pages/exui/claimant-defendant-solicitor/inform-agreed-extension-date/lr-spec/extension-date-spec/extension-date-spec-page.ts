import { Page } from '@playwright/test';
import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import DateHelper from '../../../../../../helpers/date-helper';
import CCDCaseData from '../../../../../../models/ccd/ccd-case-data';
import ExuiPage from '../../../../exui-page/exui-page';
import DateFragment from '../../../../fragments/date/date-fragment';
import { inputs } from './extension-date-spec-content';

@AllMethodsStep()
export default class ExtensionDateSpecPage extends ExuiPage(BasePage) {
  private dateFragment: DateFragment;

  constructor(page: Page, dateFragment: DateFragment) {
    super(page);
    this.dateFragment = dateFragment;
  }
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectText(inputs.extensionDate.label),
      super.expectText(inputs.extensionDate.hintText),
    ]);
  }

  async enterDate(ccdCaseData: CCDCaseData) {
    const extensionDate = DateHelper.addToDate(ccdCaseData.respondent1ResponseDeadline, {
      days: 28,
      workingDay: true,
      addDayAfter4pm: true,
    });
    await this.dateFragment.enterDate(extensionDate, inputs.extensionDate.selectorKey);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
