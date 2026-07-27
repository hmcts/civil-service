import ExuiDashboardActions from '../../../actions/ui/exui/common/exui-dashboard-actions';
import JudgeLAActionsFactory from '../../../actions/ui/exui/judge-la/judge-la-actions-factory';
import IdamActions from '../../../actions/ui/idam/idam-actions';
import BaseExui from '../../../base/base-exui';
import { tribunalCaseworkerRegion1User } from '../../../config/users/exui-users';
import ccdEvents from '../../../constants/ccd-events/ccd-events';
import legalAdvisorSmallClaimsTrackDirectionsTask from '../../../constants/wa-tasks/legalAdvisorSmallClaimsTrackDirectionsTask';
import { AllMethodsStep } from '../../../decorators/test-steps';
import TestData from '../../../models/test-utils/test-data';
import RequestsFactory from '../../../requests/requests-factory';

@AllMethodsStep()
export default class LegalAdvisorSteps extends BaseExui {
  private judgeLaActionsFactory: JudgeLAActionsFactory;

  constructor(
    exuiDashboardActions: ExuiDashboardActions,
    idamActions: IdamActions,
    judgeLaActionsFactory: JudgeLAActionsFactory,
    requestsFactory: RequestsFactory,
    testData: TestData,
  ) {
    super(exuiDashboardActions, idamActions, requestsFactory, testData);
    this.judgeLaActionsFactory = judgeLaActionsFactory;
  }

  async LoginRegion1() {
    await super.idamActions.exuiLogin(tribunalCaseworkerRegion1User);
  }

  async SdoSmallTrackSum() {
    const { sdoActions } = this.judgeLaActionsFactory;
    await super.retryWAEvent(
      async () => {
        await sdoActions.enterJudgementYes();
        await sdoActions.selectSmallTrackSum();
        await sdoActions.smallTrackDetails();
        await sdoActions.orderPreview();
        await sdoActions.submitSdo();
      },
      async () => {
        await sdoActions.confirmSdo();
      },
      ccdEvents.CREATE_SDO,
      tribunalCaseworkerRegion1User,
      legalAdvisorSmallClaimsTrackDirectionsTask,
      { verifySuccessEvent: false }
    );
  }

  async SdoSmallTrackNoSum() {
    const { sdoActions } = this.judgeLaActionsFactory;
    await super.retryWAEvent(
      async () => {
        await sdoActions.enterJudgementNo();
        await sdoActions.selectSmallClaimNoSum();
        await sdoActions.smallTrackDetails();
        await sdoActions.orderPreview();
        await sdoActions.submitSdo();
      },
      async () => {
        await sdoActions.confirmSdo();
      },
      ccdEvents.CREATE_SDO,
      tribunalCaseworkerRegion1User,
      legalAdvisorSmallClaimsTrackDirectionsTask,
      { verifySuccessEvent: false }
    );
  }

  async SdoSmallTrackFromFastClaim() {
    const { sdoActions } = this.judgeLaActionsFactory;
    await super.retryWAEvent(
      async () => {
        await sdoActions.enterJudgementYes();
        await sdoActions.selectSmallTrackSum();
        await sdoActions.smallTrackDetails();
        await sdoActions.orderPreview();
        await sdoActions.submitSdo();
      },
      async () => {
        await sdoActions.confirmSdo();
      },
      ccdEvents.CREATE_SDO,
      tribunalCaseworkerRegion1User,
      legalAdvisorSmallClaimsTrackDirectionsTask,
      { verifySuccessEvent: false }
    );
  }

  async SdoSmallTrackDRH() {
    const { sdoActions } = this.judgeLaActionsFactory;
    await super.retryWAEvent(
      async () => {
        await sdoActions.enterJudgementYes();
        await sdoActions.selectSmallTrackDRH();
        await sdoActions.sdoDRHDetails();
        await sdoActions.orderPreview();
        await sdoActions.submitSdo();
      },
      async () => {
        await sdoActions.confirmSdo();
      },
      ccdEvents.CREATE_SDO,
      tribunalCaseworkerRegion1User,
      legalAdvisorSmallClaimsTrackDirectionsTask,
      { verifySuccessEvent: false }
    );
  }
}
