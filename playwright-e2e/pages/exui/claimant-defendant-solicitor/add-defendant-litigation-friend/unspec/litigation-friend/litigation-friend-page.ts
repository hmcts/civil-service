import { Page } from '@playwright/test';
import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ExuiPage from '../../../../exui-page/exui-page';
import LitigationFriendFragment from '../../../../fragments/litigation-friend/litigation-friend-fragment';
import { radioButtons, subheadings } from './litigation-friend-content';
import CCDCaseData from '../../../../../../models/ccd/ccd-case-data';

@AllMethodsStep()
export default class LitigationFriendPage extends ExuiPage(BasePage) {
  private litigationFriendFragment: LitigationFriendFragment;

  constructor(page: Page, litigationFriendFragment: LitigationFriendFragment) {
    super(page);
    this.litigationFriendFragment = litigationFriendFragment;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectSubheading(subheadings.litigationDetails),
      this.litigationFriendFragment.verifyContent(),
      super.expectText(radioButtons.address.label),
    ]);
  }

  async enterLitigationFriendDetails() {
    await this.litigationFriendFragment.enterLitigationFriendDetails();
    await this.litigationFriendFragment.chooseNoSameAddress();
    await this.litigationFriendFragment.uploadCertificateOfSuitability();
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
