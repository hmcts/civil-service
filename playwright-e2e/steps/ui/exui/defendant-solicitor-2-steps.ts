import ExuiDashboardActions from '../../../actions/ui/exui/common/exui-dashboard-actions';
import DefendantActionsFactory from '../../../actions/ui/exui/defendant-solicitor/defendant-actions-factory';
import IdamActions from '../../../actions/ui/idam/idam-actions';
import BaseExui from '../../../base/base-exui';
import { defendantSolicitor2User } from '../../../config/users/exui-users';
import ccdEvents from '../../../constants/ccd-events';
import { AllMethodsStep } from '../../../decorators/test-steps';
import TestData from '../../../models/test-data';
import RequestsFactory from '../../../requests/requests-factory';

@AllMethodsStep()
export default class DefendantSolicitor2Steps extends BaseExui {
  private defendantActionsFactory: DefendantActionsFactory;

  constructor(
    exuiDashboardActions: ExuiDashboardActions,
    idamActions: IdamActions,
    defendantActionsFactory: DefendantActionsFactory,
    requestsFactory: RequestsFactory,
    testData: TestData,
  ) {
    super(exuiDashboardActions, idamActions, requestsFactory, testData);
    this.defendantActionsFactory = defendantActionsFactory;
  }

  async Login() {
    await super.idamActions.exuiLogin(defendantSolicitor2User);
  }

  async AddLitigationFriend() {
    const { addDefendantLitigationFriendActions } = this.defendantActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await addDefendantLitigationFriendActions.defendant2LitigationFriend();
        await addDefendantLitigationFriendActions.submitAddDefendantLitigationFriend();
      },
      async () => {
        await addDefendantLitigationFriendActions.confirmAddDefendantLitigationFriend();
      },
      ccdEvents.ADD_DEFENDANT_LITIGATION_FRIEND,

      { verifySuccessEvent: false },
    );
  }

  async RespondSmallTrackFullDefence1v2DS() {
    const { defendantResponseActions } = this.defendantActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await defendantResponseActions.confirmDetailsDS2();
        await defendantResponseActions.respondentResponseTypeDS2();
        await defendantResponseActions.solicitorReferencesDefendantResponseDS2();
        await defendantResponseActions.uploadDefendantResponseDS2();
        await defendantResponseActions.dqSmallTrackDS2();
        await defendantResponseActions.dqDS2();
        await defendantResponseActions.statementOfTruthDS2();
        await defendantResponseActions.submitDefendantResponse();
      },
      async () => {
        await defendantResponseActions.confirmDefendantResponse();
      },
      ccdEvents.DEFENDANT_RESPONSE,

      { verifySuccessEvent: false },
    );
  }

  async AcknowledgeClaimFullDefence() {
    const { acknowlegdeClaimActions } = this.defendantActionsFactory;
    await this.retryExuiEvent(
      async () => {
        await acknowlegdeClaimActions.confirmNameAndAddress();
        await acknowlegdeClaimActions.responseIntentionDS2();
        await acknowlegdeClaimActions.solicitorReferencesAcknowledgeClaimDS2();
        await acknowlegdeClaimActions.submitAcknowledgeClaim();
      },
      async () => {
        await acknowlegdeClaimActions.confirmAcknowledgeClaimDS2();
      },
      ccdEvents.ACKNOWLEDGE_CLAIM,

      { verifySuccessEvent: false },
    );
  }
}
