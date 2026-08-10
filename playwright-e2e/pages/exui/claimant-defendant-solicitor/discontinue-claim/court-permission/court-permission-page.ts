import { Page } from '@playwright/test';
import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import { headings, radioButtons } from './court-permission-content';
import { getFormattedCaseId } from '../../../mixin-pages/exui-page/exui-content';

@AllMethodsStep()
export default class CourtPermissionPage extends ExuiPage(BasePage) {
  constructor(page: Page) {
    super(page);
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.expectText(headings.discontinueThisClaim),
      super.expectHeading(headings.permissionFromCourt),
      super.expectHeading(getFormattedCaseId(ccdCaseData?.id!), { exact: false }),
      super.expectHeading(ccdCaseData?.caseNamePublic!, { exact: false }),
      super.expectLegend(radioButtons.label),
      super.expectLegend(radioButtons.hintText),
      super.expectRadioLabel(radioButtons.yes.label, radioButtons.yes.selector),
      super.expectRadioLabel(radioButtons.no.label, radioButtons.no.selector),
    ]);
  }

  async selectPermissionRequiredYes() {
    await super.clickBySelector(radioButtons.yes.selector);
  }

  async selectPermissionRequiredNo() {
    await super.clickBySelector(radioButtons.no.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
