import ExuiDashboardActions from '../../../actions/ui/exui/common/exui-dashboard-actions';
import HearingCenterAdminActionsFactory from '../../../actions/ui/exui/hearing-center-admin/hearing-center-admin-actions-factory';
import IdamActions from '../../../actions/ui/idam/idam-actions';
import BaseExui from '../../../base/base-exui';
import {
  hearingCenterAdminRegion1User,
  hearingCenterAdminRegion2User,
} from '../../../config/users/exui-users';
import ccdEvents from '../../../constants/ccd-events/ccd-events';
import judgmentOnlineSetAsideTakeCaseOffline from '../../../constants/wa-tasks/judgmentOnlineSetAsideTakeCaseOffline';
import { AllMethodsStep } from '../../../decorators/test-steps';
import TestData from '../../../models/test-utils/test-data';
import RequestsFactory from '../../../requests/requests-factory';

@AllMethodsStep()
export default class HearingCenterAdminSpecSteps extends BaseExui {
  private hearingCenterAdminActionsFactory: HearingCenterAdminActionsFactory;

  constructor(
    exuiDashboardActions: ExuiDashboardActions,
    idamActions: IdamActions,
    hearingCenterAdminActionsFactory: HearingCenterAdminActionsFactory,
    requestsFactory: RequestsFactory,
    testData: TestData,
  ) {
    super(exuiDashboardActions, idamActions, requestsFactory, testData);
    this.hearingCenterAdminActionsFactory = hearingCenterAdminActionsFactory;
  }

  async LoginRegion1() {
    await super.idamActions.exuiLogin(hearingCenterAdminRegion1User);
  }

  async LoginRegion2() {
    await super.idamActions.exuiLogin(hearingCenterAdminRegion2User);
  }

  async CreateCaseLevelCaseFlag() {
    const { createCaseFlagsSpecActions } = this.hearingCenterAdminActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await createCaseFlagsSpecActions.selectCaseLevel();
        await createCaseFlagsSpecActions.caseLevelComplexTypeCaseFlag();
      },
      async () => {},
      ccdEvents.CREATE_CASE_FLAGS,
    );
  }

  async CreateCaseLevelCaseFlag1v2DS() {
    const { createCaseFlagsSpecActions } = this.hearingCenterAdminActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await createCaseFlagsSpecActions.selectCaseLevel();
        await createCaseFlagsSpecActions.caseLevelComplexTypeCaseFlag();
      },
      async () => {},
      ccdEvents.CREATE_CASE_FLAGS,
    );
  }

  async CreateClaimant1CaseFlag() {
    const { createCaseFlagsSpecActions } = this.hearingCenterAdminActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await createCaseFlagsSpecActions.selectClaimant1();
        await createCaseFlagsSpecActions.claimant1SepcialMeasureCaseFlag();
      },
      async () => {},
      ccdEvents.CREATE_CASE_FLAGS,
    );
  }

  async CreateClaimant1CaseFlag1v2DS() {
    const { createCaseFlagsSpecActions } = this.hearingCenterAdminActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await createCaseFlagsSpecActions.selectClaimant1();
        await createCaseFlagsSpecActions.claimant1SepcialMeasureCaseFlag();
      },
      async () => {},
      ccdEvents.CREATE_CASE_FLAGS,
    );
  }

  async ManageCaseFlags() {
    const { manageCaseFlagsActions } = this.hearingCenterAdminActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await manageCaseFlagsActions.makeInactiveCaseFlag();
      },
      async () => {},
      ccdEvents.MANAGE_CASE_FLAGS,
    );
  }

  async StayCase() {
    const { stayCaseActions } = this.hearingCenterAdminActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await stayCaseActions.stayCase();
      },
      async () => {
        await stayCaseActions.confirmStayCase();
      },
      ccdEvents.STAY_CASE,
    );
  }

  async RequestReferJudgeDefenceReceived() {
    const { referJudgeDefenceReceivedActions } = this.hearingCenterAdminActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await referJudgeDefenceReceivedActions.referToJudge();
      },
      async () => {
        await referJudgeDefenceReceivedActions.confirmReferToJudge();
      },
      ccdEvents.REFER_JUDGE_DEFENCE_RECEIVED,
      { verifySuccessEvent: false },
    );
  }

  async RequestSetAsideJudgmentFollowingApplication() {
    const { setAsideJudgmentActions } = this.hearingCenterAdminActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await setAsideJudgmentActions.setAsideJudgment();
        await setAsideJudgmentActions.setAsideOrderFollowingApplication();
        await setAsideJudgmentActions.submitSetAsideJudgment();
      },
      async () => {
        await setAsideJudgmentActions.confirmSetAsideJudgment();
      },
      ccdEvents.SET_ASIDE_JUDGMENT,
      { verifySuccessEvent: false },
    );
  }

  async RequestSetAsideJudgmentFollowingDefenceReceived() {
    const { setAsideJudgmentActions } = this.hearingCenterAdminActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await setAsideJudgmentActions.setAsideJudgment();
        await setAsideJudgmentActions.setAsideOrderFollowingDefenceReceived();
        await setAsideJudgmentActions.submitSetAsideJudgment();
      },
      async () => {
        await setAsideJudgmentActions.confirmSetAsideJudgment();
      },
      ccdEvents.SET_ASIDE_JUDGMENT,
      { verifySuccessEvent: false },
    );
  }

  async RequestSetAsideJudgmentMadeInError() {
    const { setAsideJudgmentActions } = this.hearingCenterAdminActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await setAsideJudgmentActions.setAsideJudgmentMadeInError();
        await setAsideJudgmentActions.submitSetAsideJudgment();
      },
      async () => {
        await setAsideJudgmentActions.confirmSetAsideJudgment();
      },
      ccdEvents.SET_ASIDE_JUDGMENT,
      { verifySuccessEvent: false },
    );
  }

  async CaseProceedsInCaseman() {
    const { caseProceedsInCasemanActions } = this.hearingCenterAdminActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await caseProceedsInCasemanActions.caseSettled();
      },
      async () => {},
      ccdEvents.CASE_PROCEEDS_IN_CASEMAN,
      { verifySuccessEvent: false },
    );
  }

  async CaseProceedsInCasemanSpec() {
    const { caseProceedsInCasemanActions } = this.hearingCenterAdminActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await caseProceedsInCasemanActions.caseSettledSpec();
      },
      async () => {},
      ccdEvents.CASE_PROCEEDS_IN_CASEMAN,
      { verifySuccessEvent: false },
    );
  }

  async CaseProceedsInCasemanSetAsideJudgment() {
    const { caseProceedsInCasemanActions } = this.hearingCenterAdminActionsFactory;
    await super.retryWAEvent(
      async () => {
        await caseProceedsInCasemanActions.caseSettledSpec();
      },
      async () => {},
      ccdEvents.CASE_PROCEEDS_IN_CASEMAN,
      hearingCenterAdminRegion2User,
      judgmentOnlineSetAsideTakeCaseOffline,
      { verifySuccessEvent: false },
    );
  }

  async SettleClaimConsentOrder() {
    const { settleClaimActions } = this.hearingCenterAdminActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await settleClaimActions.consentOrderApproved();
        await settleClaimActions.submitSettleClaim();
      },
      async () => {
        await settleClaimActions.confirmSettleClaim();
      },
      ccdEvents.SETTLE_CLAIM,
      { verifySuccessEvent: false },
    );
  }

  async SettleClaimJudgesOrder() {
    const { settleClaimActions } = this.hearingCenterAdminActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await settleClaimActions.settledFollowingJudgesOrder();
        await settleClaimActions.submitSettleClaim();
      },
      async () => {
        await settleClaimActions.confirmSettleClaim();
      },
      ccdEvents.SETTLE_CLAIM,
      { verifySuccessEvent: false },
    );
  }
}
