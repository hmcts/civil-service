import { Page } from '@playwright/test';
import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ExuiPage from '../../../../exui-page/exui-page';
import LitigationFriendFragment from '../../../../fragments/litigation-friend/litigation-friend-fragment';
import { radioButtons, subheadings } from './second-claimant-litigation-friend-content';
import partys from '../../../../../../constants/partys';

@AllMethodsStep()
export default class SecondClaimantLitigationFriendPage extends ExuiPage(BasePage) {
  private litigationFriendFragment: LitigationFriendFragment;

  constructor(page: Page) {
    super(page);
    this.litigationFriendFragment = new LitigationFriendFragment(
      page,
      partys.CLAIMANT_2_LITIGATION_FRIEND,
    );
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
