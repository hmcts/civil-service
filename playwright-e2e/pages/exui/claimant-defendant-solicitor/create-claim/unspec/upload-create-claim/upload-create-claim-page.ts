import { Page } from '@playwright/test';
import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ExuiPage from '../../../../mixin-pages/exui-page/exui-page';
import ParticularsOfClaimFragment from '../../../../fragments/particulars-of-claim/particulars-of-claim-fragment';
import { paragraphs, subheadings } from './upload-create-claim-content';

@AllMethodsStep()
export default class UploadCreateClaimPage extends ExuiPage(BasePage) {
  private particularsOfClaimFragment: ParticularsOfClaimFragment;

  constructor(page: Page, particularsOfClaimFragment: ParticularsOfClaimFragment) {
    super(page);
    this.particularsOfClaimFragment = particularsOfClaimFragment;
  }

  async verifyContent() {
    await super.runVerifications([
      super.verifyHeadings(),
      super.expectText(paragraphs.descriptionText1),
      super.expectText(paragraphs.descriptionText2),
      this.particularsOfClaimFragment.verifyContent(),
      super.expectSubheading(subheadings.medicalReports),
    ]);
  }

  async uploadDocuments() {
    await this.particularsOfClaimFragment.uploadDocuments();
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
