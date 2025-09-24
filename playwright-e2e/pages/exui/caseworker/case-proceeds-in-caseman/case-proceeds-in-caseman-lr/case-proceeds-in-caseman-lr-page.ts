import { Page } from '@playwright/test';
import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import DateHelper from '../../../../../helpers/date-helper';
import CCDCaseData from '../../../../../models/ccd/ccd-case-data';
import ExuiPage from '../../../exui-page/exui-page';
import DateFragment from '../../../fragments/date/date-fragment';
import { inputs, radioButtons } from './case-proceeds-in-caseman-lr-content';

@AllMethodsStep()
export default class CaseProceedsInCasemanLRPage extends ExuiPage(BasePage) {
  private dateFragment: DateFragment;

  constructor(page: Page, dateFragment: DateFragment) {
    super(page);
    this.dateFragment = dateFragment;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectLegend(inputs.date.label),
      super.expectLabel(radioButtons.reasons.application.label),
      super.expectLabel(radioButtons.reasons.judgment.label),
      super.expectLabel(radioButtons.reasons.caseSettled.label),
      super.expectLabel(radioButtons.reasons.other.label),
    ]);
  }

  async enterTodayDate() {
    await this.dateFragment.enterDate(DateHelper.getToday(), inputs.date.selectorKey);
  }

  async selectApplication() {
    await super.clickBySelector(radioButtons.reasons.application.selector);
  }

  async selectJudgmentRequest() {
    await super.clickBySelector(radioButtons.reasons.judgment.selector);
  }

  async selectSolicitorDoesNotConsent() {
    await super.clickBySelector(radioButtons.reasons.judgment.selector);
  }

  async selectCaseSettled() {
    await super.clickBySelector(radioButtons.reasons.caseSettled.selector);
  }

  async selectOther() {
    await super.clickBySelector(radioButtons.reasons.other.selector);
    await super.inputText('Other reason', inputs.otherReason.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
