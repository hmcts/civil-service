import BaseTestData from '../../../../base/base-test-data';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import TestData from '../../../../models/test-data';
import AddDefendantLitigationFriendPageFactory from '../../../../pages/exui/claimant-defendant-solicitor/add-defendant-litigation-friend/add-defendant-litigation-friend-page-factory';

@AllMethodsStep()
export default class AddDefendantLitigationFriendActions extends BaseTestData {
  private addDefendantLitigationFriendPageFactory: AddDefendantLitigationFriendPageFactory;

  constructor(
    addDefendantLitigationFriendPageFactory: AddDefendantLitigationFriendPageFactory,
    testData: TestData,
  ) {
    super(testData);
    this.addDefendantLitigationFriendPageFactory = addDefendantLitigationFriendPageFactory;
  }

  async litigationFriend() {
    const { litigationFriendPage } = this.addDefendantLitigationFriendPageFactory;
    await litigationFriendPage.verifyContent(this.ccdCaseData);
    await litigationFriendPage.enterLitigationFriendDetails();
    await litigationFriendPage.submit();
  }

  async defendant2LitigationFriend() {
    const { defendant2LitigationFriendPage } = this.addDefendantLitigationFriendPageFactory;
    await defendant2LitigationFriendPage.verifyContent(this.ccdCaseData);
    await defendant2LitigationFriendPage.enterLitigationFriendDetails();
    await defendant2LitigationFriendPage.submit();
  }

  async submitAddDefendantLitigationFriend() {
    const { submitAddDefendantLitigationFriend } = this.addDefendantLitigationFriendPageFactory;
    await submitAddDefendantLitigationFriend.verifyContent(this.ccdCaseData);
    await submitAddDefendantLitigationFriend.submit();
  }

  async confirmAddDefendantLitigationFriend() {
    const { confirmAddDefendantLitigationFriend } = this.addDefendantLitigationFriendPageFactory;
    await confirmAddDefendantLitigationFriend.verifyContent(this.ccdCaseData);
    await confirmAddDefendantLitigationFriend.submit();
  }
}
