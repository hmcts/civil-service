import BaseTestData from '../../../../base/base-test-data';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import TestData from '../../../../models/test-utils/test-data';
import SettleClaimMarkPaidFullPageFactory from '../../../../pages/exui/claimant-defendant-solicitor/settle-claim-mark-paid-full/settle-claim-mark-paid-full-page-factory';

@AllMethodsStep()
export default class SettleClaimMarkPaidFullActions extends BaseTestData {
  private settleClaimPageFactory: SettleClaimMarkPaidFullPageFactory;

  constructor(settleClaimPageFactory: SettleClaimMarkPaidFullPageFactory, testData: TestData) {
    super(testData);
    this.settleClaimPageFactory = settleClaimPageFactory;
  }

  async singleClaimant() {
    const { singleClaimantPage } = this.settleClaimPageFactory;
    await singleClaimantPage.verifyContent(this.ccdCaseData);
    await singleClaimantPage.selectYes();
    await singleClaimantPage.submit();
  }

  async multipleClaimants() {
    const { multipleClaimantsPage } = this.settleClaimPageFactory;
    await multipleClaimantsPage.verifyContent(this.ccdCaseData);
    await multipleClaimantsPage.selectYes();
    await multipleClaimantsPage.submit();
  }

  async submitSettleClaim() {
    const { submitSettleClaimPage } = this.settleClaimPageFactory;
    await submitSettleClaimPage.verifyContent(this.ccdCaseData);
    await submitSettleClaimPage.submit();
  }

  async confirmSettleClaimMarkPaidFull() {
    const { confirmSettleClaimMarkPaidFullPage } = this.settleClaimPageFactory;
    await confirmSettleClaimMarkPaidFullPage.verifyContent(this.ccdCaseData);
    await confirmSettleClaimMarkPaidFullPage.submit();
  }
}
