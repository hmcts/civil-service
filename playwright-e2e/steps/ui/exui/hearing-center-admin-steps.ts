import ExuiDashboardActions from '../../../actions/ui/exui/common/exui-dashboard-actions';
import HearingCenterAdminActionsFactory from '../../../actions/ui/exui/hearing-center-admin/hearing-center-admin-actions-factory';
import IdamActions from '../../../actions/ui/idam/idam-actions';
import BaseExui from '../../../base/base-exui';
import {
  hearingCenterAdminRegion1User,
  hearingCenterAdminRegion2User,
} from '../../../config/users/exui-users';
import ccdEvents from '../../../constants/ccd-events/ccd-events';
import { AllMethodsStep } from '../../../decorators/test-steps';
import TestData from '../../../models/test-utils/test-data';
import RequestsFactory from '../../../requests/requests-factory';

@AllMethodsStep()
export default class HearingCenterAdminSteps extends BaseExui {
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
    const { createCaseFlagsActions } = this.hearingCenterAdminActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await createCaseFlagsActions.selectCaseLevel();
        await createCaseFlagsActions.caseLevelComplexCaseCaseFlag();
      },
      async () => {},
      ccdEvents.CREATE_CASE_FLAGS,
    );
  }

  async CreateCaseLevelCaseFlag1v2DS() {
    const { createCaseFlagsActions } = this.hearingCenterAdminActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await createCaseFlagsActions.selectCaseLevel();
        await createCaseFlagsActions.caseLevelComplexCaseCaseFlag();
      },
      async () => {},
      ccdEvents.CREATE_CASE_FLAGS,
    );
  }

  async CreateClaimant1CaseFlag() {
    const { createCaseFlagsActions } = this.hearingCenterAdminActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await createCaseFlagsActions.selectClaimant1();
        await createCaseFlagsActions.claimant1SpecialMeasureCaseFlag();
      },
      async () => {},
      ccdEvents.CREATE_CASE_FLAGS,
    );
  }

  async CreateClaimant1CaseFlag1v2DS() {
    const { createCaseFlagsActions } = this.hearingCenterAdminActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await createCaseFlagsActions.selectClaimant1();
        await createCaseFlagsActions.claimant1SpecialMeasureCaseFlag();
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

  async ManageStayRequestUpdate() {
    const { manageStayActions } = this.hearingCenterAdminActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await manageStayActions.manageStayOptionsRequestUpdate();
        await manageStayActions.manageStayRequestUpdate();
        await manageStayActions.submitManageStay();
      },
      async () => {
        await manageStayActions.confirmManageStayRequestUpdate();
      },
      ccdEvents.MANAGE_STAY,
    );
  }

  async ManageStayLiftStay() {
    const { manageStayActions } = this.hearingCenterAdminActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await manageStayActions.manageStayOptionsLiftStay();
        await manageStayActions.manageStayLiftStayJudicialReferralInMediation();
        await manageStayActions.submitManageStay();
      },
      async () => {
        await manageStayActions.confirmManageStayLiftStay();
      },
      ccdEvents.MANAGE_STAY,
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

  async TransferOnlineCase() {
    const { transferOnlineCaseActions } = this.hearingCenterAdminActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await transferOnlineCaseActions.transferOnlineCase();
        await transferOnlineCaseActions.submitTransferOnlineCase();
      },
      async () => {
        await transferOnlineCaseActions.confirm();
      },
      ccdEvents.TRANSFER_ONLINE_CASE,
      { verifySuccessEvent: false },
    );
  }

  async ScheduleHearingSmall() {
    const { hearingScheduledActions } = this.hearingCenterAdminActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await hearingScheduledActions.hearingNoticeSmallClaim();
        await hearingScheduledActions.listingOrRelisting();
        await hearingScheduledActions.hearingDetails();
        await hearingScheduledActions.hearingInformation();
        await hearingScheduledActions.submitHearingScheduled();
      },
      async () => {
        await hearingScheduledActions.confirm();
      },
      ccdEvents.HEARING_SCHEDULED,
      { verifySuccessEvent: false },
    );
  }

  async RequestNewHearing() {
    const { requestHearingActions } = this.hearingCenterAdminActionsFactory;
    await super.retryHearingEvent(
      async () => {
        await requestHearingActions.requestNewHearing();
        await requestHearingActions.checkRequirements();
        await requestHearingActions.addHearingFacilities();
        await requestHearingActions.addStage();
        await requestHearingActions.addAttendance();
        await requestHearingActions.addVenue();
        await requestHearingActions.addJudge();
        await requestHearingActions.addTiming();
        await requestHearingActions.enterAdditionalInstructions();
        await requestHearingActions.submitHearing();
      },
      async () => {
        await requestHearingActions.confirmHearing();
      },
    );
  }

  async UpdateHearing() {
    const { requestHearingActions } = this.hearingCenterAdminActionsFactory;
    await super.retryHearingEvent(
      async () => {
        await requestHearingActions.viewDetails();
        await requestHearingActions.editHearing();
        await requestHearingActions.changeAdditionalFacilities();
        await requestHearingActions.updateAdditionalFacilities();
        await requestHearingActions.changeJudgeTypes();
        await requestHearingActions.updateJudgeTypes();
        await requestHearingActions.changeAttendance();
        await requestHearingActions.updateAttendance();
        await requestHearingActions.changeTimings();
        await requestHearingActions.updateTimings();
        await requestHearingActions.changeAdditionalInstructions();
        await requestHearingActions.updateAdditionalInstructions();
        await requestHearingActions.submitUpdatedRequest();
        await requestHearingActions.hearingChangeReason();
      },
      async () => {
        await requestHearingActions.confirmHearing();
      },
    );
  }

  async CancelHearing() {
    const { requestHearingActions } = this.hearingCenterAdminActionsFactory;
    await super.retryHearingEvent(
      async () => {
        await requestHearingActions.cancelHearing();
        await requestHearingActions.cancelHearingListedInError();
      },
      async () => {},
    );
  }
}
