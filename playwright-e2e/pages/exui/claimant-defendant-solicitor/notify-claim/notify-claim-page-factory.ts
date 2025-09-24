import BasePageFactory from '../../../../base/base-page-factory';
import partys from '../../../../constants/partys';
import AccessGrantedWarningPage from './access-granted-warning/access-granted-warning-page';
import CertificateOfService1NotifyClaimPage from './certificate-of-service-1-notify-claim/certificate-of-service-1-notify-claim-page';
import CertificateOfService2NotifyClaimPage from './certificate-of-service-2-notify-claim/certificate-of-service-2-notify-claim-page';
import ConfirmNotifyClaimCOSPage from './confirm-notify-claim/confirm-notify-claim-cos-page';
import ConfirmNotifyClaimPage from './confirm-notify-claim/confirm-notify-claim-page';
import DefendantSolicitorToNotifyPage from './defendant-solicitor-to-notify/defendant-solicitor-to-notify-page';
import SubmitNotifyClaimPage from './submit-notify-claim/submit-notify-claim-page';
import CertificateOfServiceNotifyClaimFragment from '../../fragments/certificate-of-service-notify-claim/certificate-of-service-notify-claim-fragment';

export default class NotifyClaimPageFactory extends BasePageFactory {
  get defendantSolicitorToNotify() {
    return new DefendantSolicitorToNotifyPage(this.page);
  }

  get accessGrantedWarningPage() {
    return new AccessGrantedWarningPage(this.page);
  }

  get submitNotifyClaimPage() {
    return new SubmitNotifyClaimPage(this.page);
  }

  get confirmNotifyClaimPage() {
    return new ConfirmNotifyClaimPage(this.page);
  }

  get certificateOfService1NotifyClaimPage() {
    const certificateOfServiceNotifyClaimFragment = new CertificateOfServiceNotifyClaimFragment(
      this.page,
      partys.DEFENDANT_1,
    );
    return new CertificateOfService1NotifyClaimPage(
      certificateOfServiceNotifyClaimFragment,
      this.page,
    );
  }

  get certificateOfService2NotifyClaimPage() {
    const certificateOfServiceNotifyClaimFragment = new CertificateOfServiceNotifyClaimFragment(
      this.page,
      partys.DEFENDANT_2,
    );
    return new CertificateOfService2NotifyClaimPage(
      certificateOfServiceNotifyClaimFragment,
      this.page,
    );
  }

  get confirmNotifyClaimCOSPage() {
    return new ConfirmNotifyClaimCOSPage(this.page);
  }
}
