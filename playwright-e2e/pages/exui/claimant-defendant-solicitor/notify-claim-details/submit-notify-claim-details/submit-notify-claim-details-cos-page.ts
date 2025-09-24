import { Page } from '@playwright/test';
import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CertificateOfServiceNotifyClaimDetailsSubmitFragment from '../../../fragments/certificate-of-service-notify-claim-details-submit/certificate-of-service-notify-claim-details-submit-fragment';
import ExuiPage from '../../../exui-page/exui-page';
import CCDCaseData from '../../../../../models/ccd/ccd-case-data';

@AllMethodsStep()
export default class SubmitNotifyClaimDetailsCOSPage extends ExuiPage(BasePage) {
  private certificateOfServiceNotifyClaimDetailsSubmitFragment: CertificateOfServiceNotifyClaimDetailsSubmitFragment;

  constructor(
    certificateOfServiceSubmitFragment: CertificateOfServiceNotifyClaimDetailsSubmitFragment,
    page: Page,
  ) {
    super(page);
    this.certificateOfServiceNotifyClaimDetailsSubmitFragment = certificateOfServiceSubmitFragment;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      this.certificateOfServiceNotifyClaimDetailsSubmitFragment.verifyContent(),
      this.certificateOfServiceNotifyClaimDetailsSubmitFragment.verifyDefendant1Answers(),
      this.certificateOfServiceNotifyClaimDetailsSubmitFragment.verifyDefendant2Answers(),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
