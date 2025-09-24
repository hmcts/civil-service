import { Page } from '@playwright/test';
import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ExuiPage from '../../../../exui-page/exui-page';
import LitigationFriendFragment from '../../../../fragments/litigation-friend/litigation-friend-fragment';
import { radioButtons } from './claimant-litigation-friend-content';

@AllMethodsStep()
export default class ClaimantLitigationFriendPage extends ExuiPage(BasePage) {
  private litigationFriendFragment: LitigationFriendFragment;

  constructor(page: Page, litigationFriendFragment: LitigationFriendFragment) {
    super(page);
    this.litigationFriendFragment = litigationFriendFragment;
  }

  async verifyContent() {
    await super.runVerifications([
      super.verifyHeadings(),
      super.expectLegend(radioButtons.litigationFriendRequired.label),
      super.expectRadioYesLabel(radioButtons.litigationFriendRequired.yes.selector),
      super.expectRadioNoLabel(radioButtons.litigationFriendRequired.no.selector),
    ]);
  }

  async selectNo() {
    await super.clickBySelector(radioButtons.litigationFriendRequired.no.selector);
  }

  async selectYes() {
    await super.clickBySelector(radioButtons.litigationFriendRequired.yes.selector);
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
