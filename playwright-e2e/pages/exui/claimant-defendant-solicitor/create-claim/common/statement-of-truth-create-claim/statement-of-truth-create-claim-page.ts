import { Page } from '@playwright/test';
import BasePage from '../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import ExuiPage from '../../../../exui-page/exui-page.ts';
import StatementOfTruthFragment from '../../../../fragments/statement-of-truth/statement-of-truth-fragment.ts';

@AllMethodsStep()
export default class StatementOfTruthCreateClaimPage extends ExuiPage(BasePage) {
  private statementOfTruthFragment: StatementOfTruthFragment;

  constructor(page: Page, statementOfTruthFragment: StatementOfTruthFragment) {
    super(page);
    this.statementOfTruthFragment = statementOfTruthFragment;
  }

  async verifyContent() {
    await super.runVerifications([this.statementOfTruthFragment.verifyContent()]);
  }

  async enterDetails() {
    await this.statementOfTruthFragment.enterDetails();
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
