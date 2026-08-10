import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import {
  confirmationHeading,
  subheadings,
  paragraphs,
} from './confirm-decision-on-reconsideration-request-content';

@AllMethodsStep()
export default class ConfirmDecisionOnReconsiderationRequestCreateSdoPage extends ExuiPage(
  BasePage,
) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectHeading(confirmationHeading),
      super.expectSubheading(subheadings.createSdo, { headingLevel: 3 }),
      super.expectText(paragraphs.createSdo),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
