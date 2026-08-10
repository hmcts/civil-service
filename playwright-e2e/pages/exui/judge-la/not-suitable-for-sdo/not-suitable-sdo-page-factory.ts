import BasePageFactory from '../../../../base/base-page-factory';
import NotSuitableSdoPage from './not-suitable-sdo/not-suitable-sdo-page';
import SubmitNotSuitableSdoPage from './submit-not-suitable-sdo/submit-not-suitable-sdo-page';
import ConfirmOtherReasonNotSuitableSdoPage from './confirm-not-suitable-sdo/confirm-other-reason-not-suitable-sdo-page';
import ConfirmTransferCaseNotSuitableSdoPage from './confirm-not-suitable-sdo/confirm-transfer-case-not-suitable-sdo-page';

export default class NotSuitableSdoPageFactory extends BasePageFactory {
  get notSuitableSdoPage() {
    return new NotSuitableSdoPage(this.page);
  }

  get submitNotSuitableSdoPage() {
    return new SubmitNotSuitableSdoPage(this.page);
  }

  get confirmOtherReasonNotSuitableSdoPage() {
    return new ConfirmOtherReasonNotSuitableSdoPage(this.page);
  }

  get confirmTransferCaseNotSuitableSdoPage() {
    return new ConfirmTransferCaseNotSuitableSdoPage(this.page);
  }
}
