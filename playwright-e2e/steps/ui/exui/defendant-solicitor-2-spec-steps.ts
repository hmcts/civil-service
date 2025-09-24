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
export default class DefendantSolicitor2SpecSteps extends BaseExui {
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

  async RespondSmallTrackFullDefence1v2DS() {
    const { defendantResponseSpecActions } = this.defendantActionsFactory;
    await this.retryExuiEvent(
      async () => {
        await defendantResponseSpecActions.respondentChecklist();
        await defendantResponseSpecActions.responseConfirmNameAddressDS2();
        await defendantResponseSpecActions.responseConfirmDetailsDS2();
        await defendantResponseSpecActions.respondentResponseTypeSpecDS2();
        await defendantResponseSpecActions.defenceRouteDS2();
        await defendantResponseSpecActions.uploadDefendantResponseSpecDS2();
        await defendantResponseSpecActions.timelineDS2();
        await defendantResponseSpecActions.mediationDS2();
        await defendantResponseSpecActions.dqSmallTrackDS2();
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
