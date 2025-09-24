import { Page } from '@playwright/test';
import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CertificateOfServiceNotifyClaimDetailsFragment from '../../../fragments/certificate-of-service-notify-claim-details/certificate-of-service-notify-claim-details-fragment';
import ExuiPage from '../../../exui-page/exui-page';
import CCDCaseData from '../../../../../models/ccd/ccd-case-data';

@AllMethodsStep()
export default class CertificateOfService2NotifyClaimDetailsPage extends ExuiPage(BasePage) {
  private certificateOfServiceNotifyClaimDetailsFragment: CertificateOfServiceNotifyClaimDetailsFragment;

  constructor(
    certificateOfServiceNotifyClaimDetailsFragment: CertificateOfServiceNotifyClaimDetailsFragment,
    page: Page,
  ) {
    super(page);
    this.certificateOfServiceNotifyClaimDetailsFragment =
      certificateOfServiceNotifyClaimDetailsFragment;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
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
