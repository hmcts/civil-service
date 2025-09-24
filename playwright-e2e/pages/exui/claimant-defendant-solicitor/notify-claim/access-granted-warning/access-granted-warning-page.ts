import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd/ccd-case-data';
import ExuiPage from '../../../exui-page/exui-page';
import { paragraphs } from './access-granted-warning-content';

@AllMethodsStep()
export default class AccessGrantedWarningPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectText(paragraphs.descriptionText),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
