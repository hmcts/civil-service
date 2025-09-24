import BasePage from '../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../../exui-page/exui-page.ts';
import { radioButtons } from './fixed-costs-on-entry-content.ts';
import { getFormattedCaseId } from '../../../../exui-page/exui-content.ts';
import YesOrNoFragment from '../../../../fragments/yes-or-no/yes-or-no-fragment.ts';
import { Page } from '@playwright/test';

@AllMethodsStep()
export default class FixedCostsOnEntryPage extends ExuiPage(BasePage) {
  private yesOrNoFragment: YesOrNoFragment;

  constructor(page: Page, yesOrNoFragment: YesOrNoFragment) {
    super(page);
    this.yesOrNoFragment = yesOrNoFragment;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectHeading(getFormattedCaseId(ccdCaseData.id), { exact: false }),
      super.expectHeading(ccdCaseData.caseNamePublic, { exact: false }),
      super.expectLegend(radioButtons.claimFixedCosts.label),
      this.yesOrNoFragment.verifyContent(radioButtons.claimFixedCosts.selectorKey),
    ]);
  }

  async selectYesClaimFixedCosts() {
    await this.yesOrNoFragment.selectYes(radioButtons.claimFixedCosts.selectorKey);
  }

  async selectNoClaimFixedCosts() {
    await this.yesOrNoFragment.selectNo(radioButtons.claimFixedCosts.selectorKey);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
