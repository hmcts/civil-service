import BasePageActionsFactory from '../../../../base/base-page-actions-factory';
import AcknowledgeClaimPageFactory from '../../../../pages/exui/claimant-defendant-solicitor/acknowledge-claim/acknowledge-claim-page-factory';
import AddDefendantLitigationFriendPageFactory from '../../../../pages/exui/claimant-defendant-solicitor/add-defendant-litigation-friend/add-defendant-litigation-friend-page-factory';
import DefendantResponsePageFactory from '../../../../pages/exui/claimant-defendant-solicitor/response/defendant-response/defendant-response-page-factory';
import AcknowledgeClaimActions from './acknowledge-claim-actions';
import AddDefendantLitigationFriendActions from './add-defendant-litigation-friend-actions';
import DefendantResponseActions from './defendant-response/defendant-response-actions';
import DefendantResponseSpecActions from './defendant-response/defendant-response-spec-actions';

export default class DefendantActionsFactory extends BasePageActionsFactory {
  get defendantResponseActions() {
    return new DefendantResponseActions(new DefendantResponsePageFactory(this.page), this.testData);
  }

  get defendantResponseSpecActions() {
    return new DefendantResponseSpecActions(
      new DefendantResponsePageFactory(this.page),
      this.testData,
    );
  }

  get addDefendantLitigationFriendActions() {
    return new AddDefendantLitigationFriendActions(
      new AddDefendantLitigationFriendPageFactory(this.page),
      this.testData,
    );
  }

  get acknowlegdeClaimActions() {
    return new AcknowledgeClaimActions(new AcknowledgeClaimPageFactory(this.page), this.testData);
  }
}
