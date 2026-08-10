import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import { confirmationHeading, paragraphs, subheading } from './confirm-sdo-content';

@AllMethodsStep()
export default class ConfirmSdoPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectHeading(confirmationHeading),
      super.expectSubheading(`${subheading} ${ccdCaseData.legacyCaseReference}`),
      super.expectText(paragraphs.paragraph1),
      super.expectText(paragraphs.paragraph2),
      super.expectText(paragraphs.paragraph3),
      super.expectText(paragraphs.paragraph4),
    ]);
  }
  async submit() {
    await super.retryClickSubmit();
  }
}
