import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import { confirmationHeading, paragraphs, subheading } from './confirm-not-suitable-sdo-content';

@AllMethodsStep()
export default class ConfirmOtherReasonNotSuitableSdoPage extends ExuiPage(BasePage) {
  
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectHeading(confirmationHeading.transferCase),
      super.expectSubheading(subheading.transferCase),
      super.expectText(paragraphs.transferCase.paragraph1),
      super.expectText(paragraphs.transferCase.paragraph2),
      super.expectText(paragraphs.transferCase.paragraph3),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
