import { Page } from '@playwright/test';
import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CertificateOfServiceNotifyClaimFragment from '../../../fragments/certificate-of-service-notify-claim/certificate-of-service-notify-claim-fragment';
import ExuiPage from '../../../exui-page/exui-page';
import CCDCaseData from '../../../../../models/ccd/ccd-case-data';

@AllMethodsStep()
export default class CertificateOfService2NotifyClaimPage extends ExuiPage(BasePage) {
  private defendant2CertificateOfServiceNotifyClaimFragment: CertificateOfServiceNotifyClaimFragment;

  constructor(
    defendant2CertificateOfServiceNotifyClaimFragment: CertificateOfServiceNotifyClaimFragment,
    page: Page,
  ) {
    super(page);
    this.defendant2CertificateOfServiceNotifyClaimFragment =
      defendant2CertificateOfServiceNotifyClaimFragment;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      this.defendant2CertificateOfServiceNotifyClaimFragment.verifyContent(),
    ]);
  }

  async fillDetails() {
    await this.defendant2CertificateOfServiceNotifyClaimFragment.fillCertificateOfService();
    await this.defendant2CertificateOfServiceNotifyClaimFragment.fillStatementOfTruth();
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
