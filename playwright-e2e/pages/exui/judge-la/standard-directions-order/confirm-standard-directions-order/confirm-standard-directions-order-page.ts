import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd/ccd-case-data';
import ExuiPage from '../../../exui-page/exui-page';
import {
  confirmationHeading,
  paragraphs,
  subheading,
} from './confirm-standard-directions-order-content';

@AllMethodsStep()
export default class ConfirmStandardDirectionsOrderPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData): Promise<void> {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectHeading(confirmationHeading),
      super.expectSubheading(subheading),
      super.expectText(paragraphs.paragraph1),
      super.expectText(paragraphs.paragraph2),
      super.expectText(paragraphs.paragraph3),
      super.expectText(paragraphs.paragraph4),
    ]);
  }
  async submit(...args: any[]): Promise<void> {
    await super.retryClickSubmit();
  }
}
