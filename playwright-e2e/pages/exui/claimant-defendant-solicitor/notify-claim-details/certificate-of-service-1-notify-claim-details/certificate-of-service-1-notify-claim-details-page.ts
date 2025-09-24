import { Page } from '@playwright/test';
import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CertificateOfServiceNotifyClaimDetailsFragment from '../../../fragments/certificate-of-service-notify-claim-details/certificate-of-service-notify-claim-details-fragment';
import ExuiPage from '../../../exui-page/exui-page';

@AllMethodsStep()
export default class CertificateOfService1NotifyClaimDetailsPage extends ExuiPage(BasePage) {
  private certificateOfServiceNotifyClaimDetailsFragment: CertificateOfServiceNotifyClaimDetailsFragment;

  constructor(
    certificateOfServiceNotifyClaimDetailsFragment: CertificateOfServiceNotifyClaimDetailsFragment,
    page: Page,
  ) {
    super(page);
    this.certificateOfServiceNotifyClaimDetailsFragment =
      certificateOfServiceNotifyClaimDetailsFragment;
  }

  async verifyContent() {
    await super.runVerifications([
      this.certificateOfServiceNotifyClaimDetailsFragment.verifyContent(),
    ]);
  }

  async fillDetails() {
    await this.certificateOfServiceNotifyClaimDetailsFragment.fillCertificateOfService();
    await this.certificateOfServiceNotifyClaimDetailsFragment.uploadSupportingEvidence();
    await this.certificateOfServiceNotifyClaimDetailsFragment.fillStatementOfTruth();
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
