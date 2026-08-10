import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import { paragraphs } from './manage-stay-judical-referral-in-mediation-content';

@AllMethodsStep()
export default class ManageStayJudicalReferralInMediationPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectText(paragraphs.liftingStay),
      super.expectText(paragraphs.judicialReferral),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
