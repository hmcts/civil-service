import BasePageFactory from '../../../../base/base-page-factory';
import DateFragment from '../../fragments/date/date-fragment';
import CaseManagementOrderPage from './case-management-order/case-management-order-page';
import disposalHearingSdoDJPage from './disposal-hearing-sdo-dj/disposal-hearing-sdo-dj-page';
import SubmitSdoDJPage from './submit-sdo-dj/submit-sdo-dj-page';
import OrderPreviewSdoDJPage from './order-preview-sdo-dj/order-preview-sdo-dj-page';
import confirmSdoDJPage from './confirm-sdo-dj/confirm-sdo-dj-page';
import TrialHearingSdoDJPage from './trial-hearing-sdo-dj/trial-hearing-sdo-dj-page';

export default class SdoDJPageFactory extends BasePageFactory {
  get caseManagementOrderPage() {
    return new CaseManagementOrderPage(this.page);
  }

  get disposalHearingSdoDJPage() {
    const dateFragment = new DateFragment(this.page);
    return new disposalHearingSdoDJPage(this.page, dateFragment);
  }

  get trialHearingSdoDJPage() {
    const dateFragment = new DateFragment(this.page);
    return new TrialHearingSdoDJPage(this.page, dateFragment);
  }

  get orderPreviewSdoDJPage() {
    return new OrderPreviewSdoDJPage(this.page);
  }

  get submitSdoDJPage() {
    return new SubmitSdoDJPage(this.page);
  }

  get confirmSdoDJPage() {
    return new confirmSdoDJPage(this.page);
  }
}
