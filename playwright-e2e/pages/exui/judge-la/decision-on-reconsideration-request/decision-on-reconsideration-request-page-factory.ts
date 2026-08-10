import BasePageFactory from '../../../../base/base-page-factory';
import ConfirmDecisionOnReconsiderationRequestUpholdOrderPage from './confirm-decision-on-reconsideration/confirm-decision-on-reconsideration-request-uphold-order-page';
import ConfirmDecisionOnReconsiderationRequestCreateSdoPage from './confirm-decision-on-reconsideration/confirm-decision-on-reconsideration-request-create-sdo-page';
import ConfirmDecisionOnReconsiderationRequestCreateGeneralOrderPage from './confirm-decision-on-reconsideration/confirm-decision-on-reconsideration-request-create-general-order-page';
import JudgeResponseToReconsiderationPage from './judge-response-to-reconsideration/judge-response-to-reconsideration-page';
import OrderPreviewDecisionOnReconsiderationRequestPage from './order-preview-decision-on-reconsideration-request/order-preview-decision-on-reconsideration-request-page';
import SubmitDecisionOnReconsiderationRequestPage from './submit-decision-on-reconsideration/submit-decision-on-reconsideration-request-page';

export default class DecisionOnReconsiderationRequestPageFactory extends BasePageFactory {
  get judgeResponseToReconsiderationPage() {
    return new JudgeResponseToReconsiderationPage(this.page);
  }

  get orderPreviewDecisionOnReconsiderationRequestPage() {
    return new OrderPreviewDecisionOnReconsiderationRequestPage(this.page);
  }

  get submitDecisionOnReconsiderationRequestPage() {
    return new SubmitDecisionOnReconsiderationRequestPage(this.page);
  }

  get confirmDecisionOnReconsiderationRequestUpholdOrderPage() {
    return new ConfirmDecisionOnReconsiderationRequestUpholdOrderPage(this.page);
  }

  get confirmDecisionOnReconsiderationRequestCreateSdoPage() {
    return new ConfirmDecisionOnReconsiderationRequestCreateSdoPage(this.page);
  }

  get confirmDecisionOnReconsiderationRequestCreateGeneralOrderPage() {
    return new ConfirmDecisionOnReconsiderationRequestCreateGeneralOrderPage(this.page);
  }
}
