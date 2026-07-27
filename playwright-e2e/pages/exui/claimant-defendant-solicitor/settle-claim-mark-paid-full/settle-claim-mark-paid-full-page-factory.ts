import BasePageFactory from '../../../../base/base-page-factory';
import ConfirmSettleClaimMarkPaidFullPage from './confirm-settle-claim-mark-paid-full/confirm-settle-claim-mark-paid-full-page';
import MultipleClaimantsPage from './multiple-claimants/multiple-claimants-page';
import SingleClaimantPage from './single-claimant/single-claimant-page';
import SubmitSettleClaimPage from './submit-settle-claim/submit-settle-claim-page';

export default class SettleClaimMarkPaidFullPageFactory extends BasePageFactory {
  get singleClaimantPage() {
    return new SingleClaimantPage(this.page);
  }

  get multipleClaimantsPage() {
    return new MultipleClaimantsPage(this.page);
  }

  get submitSettleClaimPage() {
    return new SubmitSettleClaimPage(this.page);
  }

  get confirmSettleClaimMarkPaidFullPage() {
    return new ConfirmSettleClaimMarkPaidFullPage(this.page);
  }
}
