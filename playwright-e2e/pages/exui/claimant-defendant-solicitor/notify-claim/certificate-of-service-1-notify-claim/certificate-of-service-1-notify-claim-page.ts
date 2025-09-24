import { Page } from '@playwright/test';
import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CertificateOfServiceNotifyClaimFragment from '../../../fragments/certificate-of-service-notify-claim/certificate-of-service-notify-claim-fragment';
import ExuiPage from '../../../exui-page/exui-page';
import CCDCaseData from '../../../../../models/ccd/ccd-case-data';

@AllMethodsStep()
export default class CertificateOfService1NotifyClaimPage extends ExuiPage(BasePage) {
  private defendant1CertificateOfServiceNotifyClaimFragment: CertificateOfServiceNotifyClaimFragment;

  constructor(
    defendant1CertificateOfServiceNotifyClaimFragment: CertificateOfServiceNotifyClaimFragment,
    page: Page,
  ) {
    super(page);
    this.defendant1CertificateOfServiceNotifyClaimFragment =
      defendant1CertificateOfServiceNotifyClaimFragment;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      this.defendant1CertificateOfServiceNotifyClaimFragment.verifyContent(),
    ]);
  }

  async fillDetails() {
    await this.defendant1CertificateOfServiceNotifyClaimFragment.fillCertificateOfService();
    await this.defendant1CertificateOfServiceNotifyClaimFragment.fillStatementOfTruth();
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
