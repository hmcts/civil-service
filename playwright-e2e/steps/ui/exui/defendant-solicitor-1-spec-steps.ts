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
export default class DefendantSolicitor1SpecSteps extends BaseExui {
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

  async RespondFastTrackFullDefence1v1() {
    const { defendantResponseSpecActions } = this.defendantActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await defendantResponseSpecActions.respondentChecklist();
        await defendantResponseSpecActions.responseConfirmNameAddressDS1();
        await defendantResponseSpecActions.responseConfirmDetailsDS1();
        await defendantResponseSpecActions.respondentResponseTypeSpecDS1();
        await defendantResponseSpecActions.defenceRouteDS1();
        await defendantResponseSpecActions.uploadDefendantResponseSpecDS1();
        await defendantResponseSpecActions.timelineDS1();
        await defendantResponseSpecActions.dqFastTrackDS1();
        await defendantResponseSpecActions.dqDS1();
        await defendantResponseSpecActions.applicationDS1();
        await defendantResponseSpecActions.statementOfTruthDefendantResponseDS1();
        await defendantResponseSpecActions.submitDefendantResponse();
      },
      async () => {
        await defendantResponseSpecActions.confirmDefendantResponseSpec();
      },
      ccdEvents.DEFENDANT_RESPONSE_SPEC,

      { verifySuccessEvent: false },
    );
  }

  async RespondSmallTrackFullDefence1v1() {
    const { defendantResponseSpecActions } = this.defendantActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await defendantResponseSpecActions.respondentChecklist();
        await defendantResponseSpecActions.responseConfirmNameAddressDS1();
        await defendantResponseSpecActions.responseConfirmDetailsDS1();
        await defendantResponseSpecActions.respondentResponseTypeSpecDS1();
        await defendantResponseSpecActions.defenceRouteDS1();
        await defendantResponseSpecActions.uploadDefendantResponseSpecDS1();
        await defendantResponseSpecActions.timelineDS1();
        await defendantResponseSpecActions.mediationDS1();
        await defendantResponseSpecActions.dqSmallTrackDS1();
        await defendantResponseSpecActions.statementOfTruthDefendantResponseDS1();
        await defendantResponseSpecActions.submitDefendantResponse();
      },
      async () => {
        await defendantResponseSpecActions.confirmDefendantResponseSpec();
      },
      ccdEvents.DEFENDANT_RESPONSE_SPEC,

      { verifySuccessEvent: false },
    );
  }

  async RespondSmallTrackFullDefence2v1() {
    const { defendantResponseSpecActions } = this.defendantActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await defendantResponseSpecActions.respondentChecklist();
        await defendantResponseSpecActions.responseConfirmNameAddressDS1();
        await defendantResponseSpecActions.responseConfirmDetailsDS1();
        await defendantResponseSpecActions.singleResponse2v1();
        await defendantResponseSpecActions.respondentResponseTypeSpecDS1();
        await defendantResponseSpecActions.defenceRouteDS1();
        await defendantResponseSpecActions.uploadDefendantResponseSpecDS1();
        await defendantResponseSpecActions.timelineDS1();
        await defendantResponseSpecActions.mediationDS1();
        await defendantResponseSpecActions.dqSmallTrackDS1();
        await defendantResponseSpecActions.statementOfTruthDefendantResponseDS1();
        await defendantResponseSpecActions.submitDefendantResponse();
      },
      async () => {
        await defendantResponseSpecActions.confirmDefendantResponseSpec();
      },
      ccdEvents.DEFENDANT_RESPONSE_SPEC,

      { verifySuccessEvent: false },
    );
  }

  async RespondSmallTrackFullDefence1v2SS() {
    const { defendantResponseSpecActions } = this.defendantActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await defendantResponseSpecActions.respondentChecklist();
        await defendantResponseSpecActions.responseConfirmNameAddress1v2();
        await defendantResponseSpecActions.responseConfirmDetailsDS1();
        await defendantResponseSpecActions.singleResponse();
        await defendantResponseSpecActions.respondentResponseTypeSpecDS1();
        await defendantResponseSpecActions.defenceRouteDS1();
        await defendantResponseSpecActions.uploadDefendantResponseSpecDS1();
        await defendantResponseSpecActions.timelineDS1();
        await defendantResponseSpecActions.mediationDS1();
        await defendantResponseSpecActions.dqSmallTrackDS1();
        await defendantResponseSpecActions.statementOfTruthDefendantResponseDS1();
        await defendantResponseSpecActions.submitDefendantResponse();
      },
      async () => {
        await defendantResponseSpecActions.confirm1v2SSDefendantResponseSpec();
      },
      ccdEvents.DEFENDANT_RESPONSE_SPEC,

      { verifySuccessEvent: false },
    );
  }

  async RespondSmallTrackFullDefence1v2DS() {
    const { defendantResponseSpecActions } = this.defendantActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await defendantResponseSpecActions.respondentChecklist();
        await defendantResponseSpecActions.responseConfirmNameAddressDS1();
        await defendantResponseSpecActions.responseConfirmDetailsDS1();
        await defendantResponseSpecActions.respondentResponseTypeSpecDS1();
        await defendantResponseSpecActions.defenceRouteDS1();
        await defendantResponseSpecActions.uploadDefendantResponseSpecDS1();
        await defendantResponseSpecActions.timelineDS1();
        await defendantResponseSpecActions.mediationDS1();
        await defendantResponseSpecActions.dqSmallTrackDS1();
        await defendantResponseSpecActions.statementOfTruthDefendantResponseDS1();
        await defendantResponseSpecActions.submitDefendantResponse();
      },
      async () => {
        await defendantResponseSpecActions.confirmDefendantResponseSpec();
      },
      ccdEvents.DEFENDANT_RESPONSE_SPEC,

      { verifySuccessEvent: false },
    );
  }
}
