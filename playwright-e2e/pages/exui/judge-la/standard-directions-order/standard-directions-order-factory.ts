import BasePageFactory from '../../../../base/base-page-factory';
import ClaimsTrackPage from './claims-track/claims-track-page';
import DisposalHearingPage from './disposal-hearing/disposal-hearing-page';
import ClaimsTrackSmallPage from './claims-track/claims-track-small-page';
import SdoPage from './sdo/sdo-page';
import FastTrackPage from './fast-track/fast-track-page';
import OrderPreviewPage from './order-preview/order-preview-page';
import OrderTypePage from './order-type/order-type-page';
import SmallClaimsPage from './small-claims/small-claims-page';
import DateFragment from '../../fragments/date/date-fragment';
import SdoR2FastTrackPage from './sdo-r2-fast-track/sdo-r2-fast-track-page';
import SdoR2SmallClaimsPage from './sdo-r2-small-claims/sdo-r2-small-claims-page';
import SubmitStandardDirectionsOrderPage from './submit-standard-directions-order/submit-standard-directions-order-page';
import ConfirmStandardDirectionsOrderPage from './confirm-standard-directions-order/confirm-standard-directions-order-page';

export default class StandardDirectionOrderPageFactory extends BasePageFactory {
  get sdoPage() {
    return new SdoPage(this.page);
  }

  get claimsTrackPage() {
    return new ClaimsTrackPage(this.page);
  }

  get claimsTrackSmallPage() {
    return new ClaimsTrackSmallPage(this.page);
  }

  get orderTypePage() {
    return new OrderTypePage(this.page);
  }

  get disposalHearingPage() {
    const dateFragment = new DateFragment(this.page);
    return new DisposalHearingPage(this.page, dateFragment);
  }

  get fastTrackPage() {
    const dateFragment = new DateFragment(this.page);
    return new FastTrackPage(this.page, dateFragment);
  }

  get sdoR2FastTrackPage() {
    const dateFragment = new DateFragment(this.page);
    return new SdoR2FastTrackPage(this.page, dateFragment);
  }

  get orderPreviewPage() {
    return new OrderPreviewPage(this.page);
  }

  get smallClaimsPage() {
    const dateFragment = new DateFragment(this.page);
    return new SmallClaimsPage(this.page, dateFragment);
  }

  get smallClaimsDisputeResolutionHearingPage() {
    const dateFragment = new DateFragment(this.page);
    return new SdoR2SmallClaimsPage(this.page, dateFragment);
  }

  get submitStandardDirectionsOrderPage() {
    return new SubmitStandardDirectionsOrderPage(this.page);
  }

  get confirmStandardDirectionsOrderPage() {
    return new ConfirmStandardDirectionsOrderPage(this.page);
  }
}
