import { Page } from '@playwright/test';
import BasePage from '../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import ExuiPage from '../../../../exui-page/exui-page';
import YesOrNoFragment from '../../../../fragments/yes-or-no/yes-or-no-fragment.ts';
import { radioButtons } from './add-another-claimant-content.ts';

@AllMethodsStep()
export default class AddAnotherClaimantPage extends ExuiPage(BasePage) {
  private yesOrNoFragment: YesOrNoFragment;

  constructor(page: Page, yesOrNoFragment: YesOrNoFragment) {
    super(page);
    this.yesOrNoFragment = yesOrNoFragment;
  }

  async verifyContent() {
    await super.runVerifications([
      super.verifyHeadings(),
      super.expectLegend(radioButtons.addAnotherClaimant.label, { count: 1 }),
      this.yesOrNoFragment.verifyContent(radioButtons.addAnotherClaimant.selectorKey),
    ]);
  }

  async selectYes() {
    await this.yesOrNoFragment.selectYes(radioButtons.addAnotherClaimant.selectorKey);
  }

  async selectNo() {
    await this.yesOrNoFragment.selectNo(radioButtons.addAnotherClaimant.selectorKey);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
