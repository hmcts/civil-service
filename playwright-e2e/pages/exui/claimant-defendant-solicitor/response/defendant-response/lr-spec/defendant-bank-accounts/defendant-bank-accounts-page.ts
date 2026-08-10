import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd-case-data.ts';
import ExuiPage from '../../../../../mixin-pages/exui-page/exui-page.ts';
import { heading } from './defendant-bank-accounts-content.ts';

@AllMethodsStep()
export default class DefendantBankAccountsPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectSubheading(heading, { exact: false }),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
