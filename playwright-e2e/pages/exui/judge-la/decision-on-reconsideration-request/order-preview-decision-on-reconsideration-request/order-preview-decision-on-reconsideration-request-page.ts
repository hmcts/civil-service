import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import { paragraph, subheading } from './order-preview-decision-on-reconsideration-request-content';

@AllMethodsStep()
export default class OrderPreviewDecisionOnReconsiderationRequestPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectSubheading(subheading),
      super.expectText(paragraph),
      super.expectButton('.pdf', { exact: false }),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
