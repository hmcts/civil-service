import BasePageFactory from '../../../../base/base-page-factory';
import ConfirmRequestForReconsiderationPage from './confirm-request-for-reconsideration/confirm-request-for-reconsideration-page';
import RequestForReconsiderationPage from './request-for-reconsideration/request-for-reconsideration-page';
import SubmitRequestForReconsiderationPage from './submit-request-for-reconsideration/submit-request-for-reconsideration-page';

export default class RequestForReconsiderationPageFactory extends BasePageFactory {
  get requestForReconsiderationPage() {
    return new RequestForReconsiderationPage(this.page);
  }

  get submitRequestForReconsiderationPage() {
    return new SubmitRequestForReconsiderationPage(this.page);
  }

  get confirmRequestForReconsiderationPage() {
    return new ConfirmRequestForReconsiderationPage(this.page);
  }
}
