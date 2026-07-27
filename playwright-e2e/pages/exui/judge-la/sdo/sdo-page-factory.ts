import BasePageFactory from '../../../../base/base-page-factory';
import ClaimsTrackPage from './claims-track/claims-track-page';
import DisposalHearingPage from './disposal-hearing-sdo/disposal-hearing-sdo-page';
import ClaimsTrackSmallPage from './claims-track/claims-track-small-page';
import SdoPage from './sdo/sdo-page';
import FastTrackPage from './fast-track/fast-track-page';
import OrderPreviewSdoPage from './order-preview-sdo/order-preview-sdo-page';
import OrderTypePage from './order-type/order-type-page';
import SmallClaimsPage from './small-claims/small-claims-page';
import DateFragment from '../../fragments/date/date-fragment';
import SdoR2FastTrackPage from './sdo-r2-fast-track/sdo-r2-fast-track-page';
import SdoR2SmallClaimsPage from './sdo-r2-small-claims/sdo-r2-small-claims-page';
import SubmitSdoPage from './submit-sdo/submit-sdo-page';
import ConfirmSdoPage from './confirm-sdo/confirm-sdo-page';

export default class SdoPageFactory extends BasePageFactory {
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

  get orderPreviewSdoPage() {
    return new OrderPreviewSdoPage(this.page);
  }

  get smallClaimsPage() {
    const dateFragment = new DateFragment(this.page);
    return new SmallClaimsPage(this.page, dateFragment);
  }

  get sdoR2SmallClaimsPage() {
    const dateFragment = new DateFragment(this.page);
    return new SdoR2SmallClaimsPage(this.page, dateFragment);
  }

  get submitSdoPage() {
    return new SubmitSdoPage(this.page);
  }

  get confirmSdoPage() {
    return new ConfirmSdoPage(this.page);
  }
}
