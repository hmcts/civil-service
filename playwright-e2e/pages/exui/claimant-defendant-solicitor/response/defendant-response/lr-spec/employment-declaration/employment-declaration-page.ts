import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd-case-data.ts';
import ExuiPage from '../../../../../mixin-pages/exui-page/exui-page.ts';
import { heading, radioButtons, inputs } from './employment-declaration-content.ts';

@AllMethodsStep()
export default class EmploymentDeclarationPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectSubheading(heading, { exact: false }),
    ]);
  }

  async selectNotEmployed() {
    await super.clickBySelector(radioButtons.employmentType.no.selector);
  }

  async selectUnemployed() {
    await super.clickBySelector(radioButtons.unemployedType.unemployed.selector);
  }

  async enterUnemploymentDuration() {
    await super.inputText('1', inputs.yearsUnemployed.selector);
    await super.inputText('0', inputs.monthsUnemployed.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
