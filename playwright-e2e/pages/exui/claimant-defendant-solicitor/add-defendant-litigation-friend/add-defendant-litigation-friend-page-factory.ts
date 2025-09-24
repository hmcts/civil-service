import BasePageFactory from '../../../../base/base-page-factory';
import partys from '../../../../constants/partys';
import ConfirmAddDefendantLitigationFriendPage from './unspec/confirm-add-defendant-litigation-friend/confirm-add-defendant-litigation-friend-page';
import LitigationFriendPage from './unspec/litigation-friend/litigation-friend-page';
import SubmitAddDefendantLitigationFriendPage from './unspec/submit-add-defendant-litigation-friend/submit-add-defendant-litigation-friend-page';
import LitigationFriendFragment from '../../fragments/litigation-friend/litigation-friend-fragment';
import Defendant2LitigationFriendPage from './unspec/defendant-2-litigation-friend/defendant-2-litigation-friend-page';

export default class AddDefendantLitigationFriendPageFactory extends BasePageFactory {
  get litigationFriendPage() {
    const litigationFriendFragment = new LitigationFriendFragment(
      this.page,
      partys.DEFENDANT_1_LITIGATION_FRIEND,
    );
    return new LitigationFriendPage(this.page, litigationFriendFragment);
  }

  get defendant2LitigationFriendPage() {
    const litigationFriendFragment = new LitigationFriendFragment(
      this.page,
      partys.DEFENDANT_2_LITIGATION_FRIEND,
    );
    return new Defendant2LitigationFriendPage(this.page, litigationFriendFragment);
  }

  get submitAddDefendantLitigationFriend() {
    return new SubmitAddDefendantLitigationFriendPage(this.page);
  }

  get confirmAddDefendantLitigationFriend() {
    return new ConfirmAddDefendantLitigationFriendPage(this.page);
  }
}
