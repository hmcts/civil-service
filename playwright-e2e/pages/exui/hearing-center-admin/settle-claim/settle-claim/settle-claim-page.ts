import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import { getFormattedCaseId } from '../../../mixin-pages/exui-page/exui-content';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import { heading, radioButtons } from './settle-claim-content';

@AllMethodsStep()
export default class SettleClaimPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.expectHeading(heading),
      super.expectHeading(getFormattedCaseId(ccdCaseData.id!), { exact: false }),
      super.expectHeading(ccdCaseData.caseNamePublic!, { exact: false }),
      super.expectLabel(radioButtons.settledFollowingJudgesOrder.label),
      super.expectLabel(radioButtons.consentOrderApproved.label),
    ]);
  }

  async selectSettledFollowingJudgesOrder() {
    await super.clickBySelector(radioButtons.settledFollowingJudgesOrder.selector);
  }

  async selectConsentOrderApproved() {
    await super.clickBySelector(radioButtons.consentOrderApproved.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
