import BaseTestData from '../../../../base/base-test-data';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import TestData from '../../../../models/test-utils/test-data';
import SettleClaimPageFactory from '../../../../pages/exui/hearing-center-admin/settle-claim/settle-claim-page-factory';
import SettleClaimMarkPaidFullPageFactory from '../../../../pages/exui/claimant-defendant-solicitor/settle-claim-mark-paid-full/settle-claim-mark-paid-full-page-factory';

@AllMethodsStep()
export default class SettleClaimActions extends BaseTestData {
  private settleClaimPageFactory: SettleClaimPageFactory;
  private settleClaimMarkPaidFullPageFactory: SettleClaimMarkPaidFullPageFactory;

  constructor(
    settleClaimPageFactory: SettleClaimPageFactory,
    settleClaimMarkPaidFullPageFactory: SettleClaimMarkPaidFullPageFactory,
    testData: TestData,
  ) {
    super(testData);
    this.settleClaimPageFactory = settleClaimPageFactory;
    this.settleClaimMarkPaidFullPageFactory = settleClaimMarkPaidFullPageFactory;
  }

  async consentOrderApproved() {
    const { settleClaimPage } = this.settleClaimPageFactory;
    await settleClaimPage.verifyContent(this.ccdCaseData);
    await settleClaimPage.selectConsentOrderApproved();
    await settleClaimPage.submit();
  }

  async settledFollowingJudgesOrder() {
    const { settleClaimPage } = this.settleClaimPageFactory;
    await settleClaimPage.verifyContent(this.ccdCaseData);
    await settleClaimPage.selectSettledFollowingJudgesOrder();
    await settleClaimPage.submit();
  }

  async submitSettleClaim() {
    const { submitSettleClaimPage } = this.settleClaimMarkPaidFullPageFactory;
    await submitSettleClaimPage.verifyContent(this.ccdCaseData);
    await submitSettleClaimPage.submit();
  }

  async confirmSettleClaim() {
    const { confirmSettleClaimPage } = this.settleClaimPageFactory;
    await confirmSettleClaimPage.verifyContent(this.ccdCaseData);
    await confirmSettleClaimPage.submit();
  }
}
