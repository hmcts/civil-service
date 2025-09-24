import ExuiDashboardActions from '../../../actions/ui/exui/common/exui-dashboard-actions';
import DefendantActionsFactory from '../../../actions/ui/exui/defendant-solicitor/defendant-actions-factory';
import IdamActions from '../../../actions/ui/idam/idam-actions';
import BaseExui from '../../../base/base-exui';
import { defendantSolicitor1User } from '../../../config/users/exui-users';
import ccdEvents from '../../../constants/ccd-events';
import { AllMethodsStep } from '../../../decorators/test-steps';
import TestData from '../../../models/test-data';
import RequestsFactory from '../../../requests/requests-factory';

@AllMethodsStep()
export default class DefendantSolicitor1Steps extends BaseExui {
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
    await super.idamActions.exuiLogin(defendantSolicitor1User);
  }

  async AddLitigationFriend() {
    const { addDefendantLitigationFriendActions } = this.defendantActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await addDefendantLitigationFriendActions.litigationFriend();
        await addDefendantLitigationFriendActions.submitAddDefendantLitigationFriend();
      },
      async () => {
        await addDefendantLitigationFriendActions.confirmAddDefendantLitigationFriend();
      },
      ccdEvents.ADD_DEFENDANT_LITIGATION_FRIEND,

      { verifySuccessEvent: false },
    );
  }

  async RespondFastTrackFullDefence1v1() {
    const { defendantResponseActions } = this.defendantActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await defendantResponseActions.confirmDetailsDS1();
        await defendantResponseActions.respondentResponseTypeDS1();
        await defendantResponseActions.solicitorReferencesDefendantResponseDS1();
        await defendantResponseActions.uploadDefendantResponseDS1();
        await defendantResponseActions.dqFastTrackDS1();
        await defendantResponseActions.dqDS1();
        await defendantResponseActions.statementOfTruthDS1();
        await defendantResponseActions.submitDefendantResponse();
      },
      async () => {
        await defendantResponseActions.confirmDefendantResponse();
      },
      ccdEvents.DEFENDANT_RESPONSE,

      { verifySuccessEvent: false },
    );
  }

  async RespondSmallTrackFullDefence1v1() {
    const { defendantResponseActions } = this.defendantActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await defendantResponseActions.confirmDetailsDS1();
        await defendantResponseActions.respondentResponseTypeDS1();
        await defendantResponseActions.solicitorReferencesDefendantResponseDS1();
        await defendantResponseActions.uploadDefendantResponseDS1();
        await defendantResponseActions.dqSmallTrackDS1();
        await defendantResponseActions.dqDS1();
        await defendantResponseActions.statementOfTruthDS1();
        await defendantResponseActions.submitDefendantResponse();
      },
      async () => {
        await defendantResponseActions.confirmDefendantResponse();
      },
      ccdEvents.DEFENDANT_RESPONSE,

      { verifySuccessEvent: false },
    );
  }

  async RespondSmallTrackFullDefence2v1() {
    const { defendantResponseActions } = this.defendantActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await defendantResponseActions.confirmDetailsDS1();
        await defendantResponseActions.respondentResponseType2v1();
        await defendantResponseActions.solicitorReferencesDefendantResponseDS1();
        await defendantResponseActions.uploadDefendantResponseDS1();
        await defendantResponseActions.dqSmallTrackDS1();
        await defendantResponseActions.dqDS1();
        await defendantResponseActions.statementOfTruthDS1();
        await defendantResponseActions.submitDefendantResponse();
      },
      async () => {
        await defendantResponseActions.confirmDefendantResponse();
      },
      ccdEvents.DEFENDANT_RESPONSE,

      { verifySuccessEvent: false },
    );
  }

  async RespondSmallTrackFullDefence1v2SS() {
    const { defendantResponseActions } = this.defendantActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await defendantResponseActions.confirmDetailsDS1();
        await defendantResponseActions.singleResponse();
        await defendantResponseActions.respondentResponseTypeDS1();
        await defendantResponseActions.solicitorReferencesDefendantResponseDS1();
        await defendantResponseActions.uploadDefendantResponseDS1();
        await defendantResponseActions.dqSmallTrackDS1();
        await defendantResponseActions.dqDS1();
        await defendantResponseActions.statementOfTruthDS1();
        await defendantResponseActions.submitDefendantResponse();
      },
      async () => {
        await defendantResponseActions.confirmDefendantResponse();
      },
      ccdEvents.DEFENDANT_RESPONSE,

      { verifySuccessEvent: false },
    );
  }

  async RespondSmallTrackFullDefence1v2DS() {
    const { defendantResponseActions } = this.defendantActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await defendantResponseActions.confirmDetailsDS1();
        await defendantResponseActions.respondentResponseTypeDS1();
        await defendantResponseActions.solicitorReferencesDefendantResponseDS1();
        await defendantResponseActions.uploadDefendantResponseDS1();
        await defendantResponseActions.dqSmallTrackDS1();
        await defendantResponseActions.dqDS1();
        await defendantResponseActions.statementOfTruthDS1();
        await defendantResponseActions.submitDefendantResponse();
      },
      async () => {
        await defendantResponseActions.confirmDefendantResponse1v2DS();
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
        await acknowlegdeClaimActions.responseIntentionDS1();
        await acknowlegdeClaimActions.solicitorReferencesAcknowledgeClaimDS1();
        await acknowlegdeClaimActions.submitAcknowledgeClaim();
      },
      async () => {
        await acknowlegdeClaimActions.confirmAcknowledgeClaimDS1();
      },
      ccdEvents.ACKNOWLEDGE_CLAIM,

      { verifySuccessEvent: false },
    );
  }

  async AcknowledgeClaimFullDefence2v1() {
    const { acknowlegdeClaimActions } = this.defendantActionsFactory;
    await this.retryExuiEvent(
      async () => {
        await acknowlegdeClaimActions.confirmNameAndAddress();
        await acknowlegdeClaimActions.responseIntention2v1();
        await acknowlegdeClaimActions.solicitorReferencesAcknowledgeClaimDS1();
        await acknowlegdeClaimActions.submitAcknowledgeClaim();
      },
      async () => {
        await acknowlegdeClaimActions.confirmAcknowledgeClaimDS1();
      },
      ccdEvents.ACKNOWLEDGE_CLAIM,

      { verifySuccessEvent: false },
    );
  }

  async AcknowledgeClaimFullDefence1v2SS() {
    const { acknowlegdeClaimActions } = this.defendantActionsFactory;
    await this.retryExuiEvent(
      async () => {
        await acknowlegdeClaimActions.confirmNameAndAddress();
        await acknowlegdeClaimActions.responseIntention1v2SS();
        await acknowlegdeClaimActions.solicitorReferencesAcknowledgeClaimDS1();
        await acknowlegdeClaimActions.submitAcknowledgeClaim();
      },
      async () => {
        await acknowlegdeClaimActions.confirmAcknowledgeClaimDS1();
      },
      ccdEvents.ACKNOWLEDGE_CLAIM,

      { verifySuccessEvent: false },
    );
  }
}
