import { Page } from '@playwright/test';
import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import DateFragment from '../../../fragments/date/date-fragment';
import DateHelper from '../../../../../helpers/date-helper.ts';
import { headings, radioButtons, inputs, selectorKeys } from './permission-granted-content';
import { getFormattedCaseId } from '../../../mixin-pages/exui-page/exui-content';

@AllMethodsStep()
export default class PermissionGrantedPage extends ExuiPage(BasePage) {
  private dateFragment: DateFragment;

  constructor(page: Page, dateFragment: DateFragment) {
    super(page);
    this.dateFragment = dateFragment;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.expectText(headings.discontinueThisClaim),
      super.expectHeading(headings.permissionGranted),
      super.expectHeading(getFormattedCaseId(ccdCaseData?.id!), { exact: false }),
      super.expectLegend(radioButtons.label),
      super.expectRadioLabel(radioButtons.yes.label, radioButtons.yes.selector),
      super.expectRadioLabel(radioButtons.no.label, radioButtons.no.selector),
    ]);
  }

  async selectPermissionGrantedYes() {
    await super.clickBySelector(radioButtons.yes.selector);
    await super.inputText('Testing', inputs.judgeName.selector);
    const permissionGrantedDate = DateHelper.subtractFromToday({ months: 6 });
    await this.dateFragment.enterDate(permissionGrantedDate, selectorKeys.permissionGrantedDate);
  }

  async submit() {
    await super.retryClickSubmit(() =>
      super.expectNoSelector(inputs.judgeName.selector, { timeout: 3000 }),
    );
  }
}
