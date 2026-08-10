import BasePageFactory from '../../../../base/base-page-factory';
import ConfirmSettleClaimPage from './confirm-settle-claim/confirm-settle-claim-page';
import SettleClaimPage from './settle-claim/settle-claim-page';

export default class SettleClaimPageFactory extends BasePageFactory {
  get settleClaimPage() {
    return new SettleClaimPage(this.page);
  }

  get confirmSettleClaimPage() {
    return new ConfirmSettleClaimPage(this.page);
  }
}
