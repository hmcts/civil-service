import { Page } from '@playwright/test';
import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd/ccd-case-data';
import ExuiPage from '../../../exui-page/exui-page';
import ParticularsOfClaimFragment from '../../../fragments/particulars-of-claim/particulars-of-claim-fragment';
import { subheadings } from './upload-notify-claim-details-content';

@AllMethodsStep()
export default class UploadNotifyClaimDetailsPage extends ExuiPage(BasePage) {
  private particularsOfClaimFragment: ParticularsOfClaimFragment;

  constructor(page: Page, particularsOfClaimFragment: ParticularsOfClaimFragment) {
    super(page);
    this.particularsOfClaimFragment = particularsOfClaimFragment;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectSubheading(subheadings.medicalReports),
      this.particularsOfClaimFragment.verifyContent(),
    ]);
  }

  async uploadDocuments() {
    await this.particularsOfClaimFragment.uploadDocuments();
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
