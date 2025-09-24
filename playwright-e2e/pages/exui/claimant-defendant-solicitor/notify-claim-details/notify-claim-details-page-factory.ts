import BasePageFactory from '../../../../base/base-page-factory';
import partys from '../../../../constants/partys';
import CertificateOfService1NotifyClaimDetailsPage from './certificate-of-service-1-notify-claim-details/certificate-of-service-1-notify-claim-details-page';
import CertificateOfService2NotifyClaimDetailsPage from './certificate-of-service-2-notify-claim-details/certificate-of-service-2-notify-claim-details-page';
import ConfirmNotifyClaimDetailsCOSPage from './confirm-notify-claim-details/confirm-notify-claim-details-cos-page';
import ConfirmNotifyClaimDetailsPage from './confirm-notify-claim-details/confirm-notify-claim-details-page';
import SelectDefendantSolicitorPage from './select-defendant-solicitor/select-defendant-solicitor-page';
import SubmitNotifyClaimDetailsCOSPage from './submit-notify-claim-details/submit-notify-claim-details-cos-page';
import SubmitNotifyClaimDetailsLIPLRPage from './submit-notify-claim-details/submit-notify-claim-details-lip-lr-page';
import SubmitNotifyClaimDetailsLIPPage from './submit-notify-claim-details/submit-notify-claim-details-lip-page';
import SubmitNotifyClaimDetailsPage from './submit-notify-claim-details/submit-notify-claim-details-page';
import UploadNotifyClaimDetailsPage from './upload-notify-claim-details/upload-notify-claim-details-page';
import CertificateOfServiceNotifyClaimDetailsSubmitFragment from '../../fragments/certificate-of-service-notify-claim-details-submit/certificate-of-service-notify-claim-details-submit-fragment';
import CertificateOfServiceNotifyClaimDetailsFragment from '../../fragments/certificate-of-service-notify-claim-details/certificate-of-service-notify-claim-details-fragment';
import ParticularsOfClaimFragment from '../../fragments/particulars-of-claim/particulars-of-claim-fragment';

export default class NotifyClaimDetailsPageFactory extends BasePageFactory {
  get selectDefendantSolicitorPage() {
    return new SelectDefendantSolicitorPage(this.page);
  }

  get uploadNotifyClaimDetailsPage() {
    const particularsOfClaimFragment = new ParticularsOfClaimFragment(this.page);
    return new UploadNotifyClaimDetailsPage(this.page, particularsOfClaimFragment);
  }

  get submitNotifyClaimDetailsPage() {
    return new SubmitNotifyClaimDetailsPage(this.page);
  }

  get submitNotifyClaimDetailsLIPPage() {
    return new SubmitNotifyClaimDetailsLIPPage(this.page);
  }

  get submitNotifyClaimDetailsLIPLRPage() {
    return new SubmitNotifyClaimDetailsLIPLRPage(this.page);
  }

  get confirmNotifyClaimDetailsPage() {
    return new ConfirmNotifyClaimDetailsPage(this.page);
  }

  get certificateOfService1NotifyClaimDetailsPage() {
    const certificateOfServiceNotifyClaimDetailsFragment =
      new CertificateOfServiceNotifyClaimDetailsFragment(this.page, partys.DEFENDANT_1);
    return new CertificateOfService1NotifyClaimDetailsPage(
      certificateOfServiceNotifyClaimDetailsFragment,
      this.page,
    );
  }

  get certificateOfService2NotifyClaimDetailsPage() {
    const certificateOfServiceNotifyClaimDetailsFragment =
      new CertificateOfServiceNotifyClaimDetailsFragment(this.page, partys.DEFENDANT_2);
    return new CertificateOfService2NotifyClaimDetailsPage(
      certificateOfServiceNotifyClaimDetailsFragment,
      this.page,
    );
  }

  get submitNotifyClaimDetailsCOSPage() {
    const certificateOfServiceNotifyClaimDetailsSubmitFragment =
      new CertificateOfServiceNotifyClaimDetailsSubmitFragment(this.page);
    return new SubmitNotifyClaimDetailsCOSPage(
      certificateOfServiceNotifyClaimDetailsSubmitFragment,
      this.page,
    );
  }

  get confirmNotifyClaimDetailsCOSPage() {
    return new ConfirmNotifyClaimDetailsCOSPage(this.page);
  }
}
