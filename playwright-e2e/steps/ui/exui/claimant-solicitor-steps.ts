import ClaimantSolicitorActionsFactory from '../../../actions/ui/exui/claimant-solicitor/claimant-solcitor-actions-factory';
import ExuiDashboardActions from '../../../actions/ui/exui/common/exui-dashboard-actions';
import IdamActions from '../../../actions/ui/idam/idam-actions';
import BaseExui from '../../../base/base-exui';
import { claimantSolicitorUser } from '../../../config/users/exui-users';
import ccdEvents from '../../../constants/ccd-events';
import { AllMethodsStep } from '../../../decorators/test-steps';
import TestData from '../../../models/test-data';
import RequestsFactory from '../../../requests/requests-factory';

@AllMethodsStep()
export default class ClaimantSolicitorSteps extends BaseExui {
  private claimantSolicitorActionsFactory: ClaimantSolicitorActionsFactory;

  constructor(
    exuiDashboardActions: ExuiDashboardActions,
    idamActions: IdamActions,
    claimantSolicitorActionsFactory: ClaimantSolicitorActionsFactory,
    requestsFactory: RequestsFactory,
    testData: TestData,
  ) {
    super(exuiDashboardActions, idamActions, requestsFactory, testData);
    this.claimantSolicitorActionsFactory = claimantSolicitorActionsFactory;
  }

  async Login() {
    await super.idamActions.exuiLogin(claimantSolicitorUser);
  }

  async CreateClaimFastTrack1v1() {
    const { createClaimActions } = this.claimantSolicitorActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await createClaimActions.eligibility();
        await createClaimActions.references();
        await createClaimActions.court();
        await createClaimActions.claimantDetails();
        await createClaimActions.noAddAnotherClaimant();
        await createClaimActions.defendantDetails();
        await createClaimActions.noAddAnotherDefendant();
        await createClaimActions.fastTrackClaimDetails();
        await createClaimActions.statementOfTruthCreateClaim();
        await createClaimActions.submitCreateClaim();
      },
      async () => {
        await createClaimActions.confirmCreateClaim();
      },
      ccdEvents.CREATE_CLAIM,

      { verifySuccessEvent: false },
    );
  }
  async CreateClaimSmallTrack1v1() {
    const { createClaimActions } = this.claimantSolicitorActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await createClaimActions.eligibility();
        await createClaimActions.references();
        await createClaimActions.court();
        await createClaimActions.claimantDetails();
        await createClaimActions.noAddAnotherClaimant();
        await createClaimActions.defendantDetails();
        await createClaimActions.noAddAnotherDefendant();
        await createClaimActions.smallTrackClaimDetails();
        await createClaimActions.statementOfTruthCreateClaim();
        await createClaimActions.submitCreateClaim();
      },
      async () => {
        await createClaimActions.confirmCreateClaim();
      },
      ccdEvents.CREATE_CLAIM,

      { verifySuccessEvent: false },
    );
  }

  async CreateClaimSmallTrack1vLIP() {
    const { createClaimActions } = this.claimantSolicitorActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await createClaimActions.eligibility();
        await createClaimActions.references();
        await createClaimActions.court();
        await createClaimActions.claimantDetails();
        await createClaimActions.noAddAnotherClaimant();
        await createClaimActions.defendantDetailsLIP(); // Placeholder for LIP-specific defendant details journey
        await createClaimActions.noAddAnotherDefendant();
        await createClaimActions.smallTrackClaimDetails();
        await createClaimActions.statementOfTruthCreateClaim();
        await createClaimActions.submitCreateClaim();
      },
      async () => {
        await createClaimActions.confirmCreateClaimLIP();
      },
      ccdEvents.CREATE_CLAIM,

      { verifySuccessEvent: false },
    );
  }

  async CreateClaimSmallTrack2v1() {
    const { createClaimActions } = this.claimantSolicitorActionsFactory;
    await this.retryExuiEvent(
      async () => {
        await createClaimActions.eligibility();
        await createClaimActions.references();
        await createClaimActions.court();
        await createClaimActions.claimantDetails();
        await createClaimActions.addAnotherClaimant();
        await createClaimActions.secondClaimant();
        await createClaimActions.secondClaimantLitigationFriend();
        await createClaimActions.defendantDetails();
        await createClaimActions.smallTrackClaimDetails();
        await createClaimActions.statementOfTruthCreateClaim();
        await createClaimActions.submitCreateClaim();
      },
      async () => {
        await createClaimActions.confirmCreateClaim();
      },
      ccdEvents.CREATE_CLAIM,

      { verifySuccessEvent: false },
    );
  }

  async CreateClaimSmallTrack1v2SS() {
    const { createClaimActions } = this.claimantSolicitorActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await createClaimActions.eligibility();
        await createClaimActions.references();
        await createClaimActions.court();
        await createClaimActions.claimantDetails();
        await createClaimActions.noAddAnotherClaimant();
        await createClaimActions.defendantDetails();
        await createClaimActions.addAnotherDefendant();
        await createClaimActions.secondDefendantSS();
        await createClaimActions.smallTrackClaimDetails();
        await createClaimActions.statementOfTruthCreateClaim();
        await createClaimActions.submitCreateClaim();
      },
      async () => {
        await createClaimActions.confirmCreateClaim();
      },
      ccdEvents.CREATE_CLAIM,

      { verifySuccessEvent: false },
    );
  }

  async CreateClaimSmallTrack1v2LIPs() {
    const { createClaimActions } = this.claimantSolicitorActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await createClaimActions.eligibility();
        await createClaimActions.references();
        await createClaimActions.court();
        await createClaimActions.claimantDetails();
        await createClaimActions.noAddAnotherClaimant();
        await createClaimActions.defendantDetailsLIP(); // First defendant (LIP)
        await createClaimActions.addAnotherDefendant();
        await createClaimActions.secondDefendantLIP(); // Second defendant (LIP)
        await createClaimActions.smallTrackClaimDetails();
        await createClaimActions.statementOfTruthCreateClaim();
        await createClaimActions.submitCreateClaim();
      },
      async () => {
        await createClaimActions.confirmCreateClaimLIP();
      },
      ccdEvents.CREATE_CLAIM,

      { verifySuccessEvent: false },
    );
  }

  async CreateClaimSmallTrack1v2LRLIP() {
    const { createClaimActions } = this.claimantSolicitorActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await createClaimActions.eligibility();
        await createClaimActions.references();
        await createClaimActions.court();
        await createClaimActions.claimantDetails();
        await createClaimActions.noAddAnotherClaimant();
        await createClaimActions.defendantDetails(); // First defendant (Legally Represented)
        await createClaimActions.addAnotherDefendant();
        await createClaimActions.secondDefendantLIP(); // Second defendant (LIP)
        await createClaimActions.smallTrackClaimDetails();
        await createClaimActions.statementOfTruthCreateClaim();
        await createClaimActions.submitCreateClaim();
      },
      async () => {
        await createClaimActions.confirmCreateClaimLIP();
      },
      ccdEvents.CREATE_CLAIM,

      { verifySuccessEvent: false },
    );
  }

  async CreateClaimSmallTrack1v2DS() {
    const { createClaimActions } = this.claimantSolicitorActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await createClaimActions.eligibility();
        await createClaimActions.references();
        await createClaimActions.court();
        await createClaimActions.claimantDetails();
        await createClaimActions.noAddAnotherClaimant();
        await createClaimActions.defendantDetails();
        await createClaimActions.addAnotherDefendant();
        await createClaimActions.secondDefendantDSdetails();
        await createClaimActions.smallTrackClaimDetails();
        await createClaimActions.statementOfTruthCreateClaim();
        await createClaimActions.submitCreateClaim();
      },
      async () => {
        await createClaimActions.confirmCreateClaim();
      },
      ccdEvents.CREATE_CLAIM,

      { verifySuccessEvent: false },
    );
  }

  async NotifyClaim() {
    const { notifyClaimActions } = this.claimantSolicitorActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await notifyClaimActions.accessGrantedWarning();
        await notifyClaimActions.submitNotifyClaim();
      },
      async () => {
        await notifyClaimActions.confirmNotifyClaim();
      },
      ccdEvents.NOTIFY_DEFENDANT_OF_CLAIM,

      { verifySuccessEvent: false },
    );
  }

  async NotifyClaim1v2DS() {
    const { notifyClaimActions } = this.claimantSolicitorActionsFactory;
    await this.retryExuiEvent(
      async () => {
        await notifyClaimActions.defendantSolicitorToNotify();
        await notifyClaimActions.accessGrantedWarning();
        await notifyClaimActions.submitNotifyClaim();
      },
      async () => {
        await notifyClaimActions.confirmNotifyClaim();
      },
      ccdEvents.NOTIFY_DEFENDANT_OF_CLAIM,

      { verifySuccessEvent: false },
    );
  }

  async NotifyClaim1v1LIP() {
    const { notifyClaimActions } = this.claimantSolicitorActionsFactory;
    await this.retryExuiEvent(
      async () => {
        await notifyClaimActions.certificateOfService1NotifyClaim();
        await notifyClaimActions.submitNotifyClaim();
      },
      async () => {
        await notifyClaimActions.confirmNotifyClaimCOS();
      },
      ccdEvents.NOTIFY_DEFENDANT_OF_CLAIM,

      { verifySuccessEvent: false },
    );
  }
  async NotifyClaim1v2LIPS() {
    const { notifyClaimActions } = this.claimantSolicitorActionsFactory;
    await this.retryExuiEvent(
      async () => {
        await notifyClaimActions.certificateOfService1NotifyClaim();
        await notifyClaimActions.certificateOfService2NotifyClaim();
        await notifyClaimActions.submitNotifyClaim();
      },
      async () => {
        await notifyClaimActions.confirmNotifyClaimCOS();
      },
      ccdEvents.NOTIFY_DEFENDANT_OF_CLAIM,

      { verifySuccessEvent: false },
    );
  }

  async NotifyClaim1v1LIP1LR() {
    const { notifyClaimActions } = this.claimantSolicitorActionsFactory;
    await this.retryExuiEvent(
      async () => {
        await notifyClaimActions.accessGrantedWarning();
        await notifyClaimActions.certificateOfService2NotifyClaim();
        await notifyClaimActions.submitNotifyClaim();
      },
      async () => {
        await notifyClaimActions.confirmNotifyClaimCOS();
      },
      ccdEvents.NOTIFY_DEFENDANT_OF_CLAIM,

      { verifySuccessEvent: false },
    );
  }

  async NotifyClaimDetails() {
    const { notifyClaimDetailsActions } = this.claimantSolicitorActionsFactory;
    await this.retryExuiEvent(
      async () => {
        await notifyClaimDetailsActions.uploadNotifyClaimDetails();
        await notifyClaimDetailsActions.submitNotifyClaimDetails();
      },
      async () => {
        await notifyClaimDetailsActions.confirmNotifyClaimDetails();
      },
      ccdEvents.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS,

      { verifySuccessEvent: false },
    );
  }

  async NotifyClaimDetails1v2DS() {
    const { notifyClaimDetailsActions } = this.claimantSolicitorActionsFactory;
    await this.retryExuiEvent(
      async () => {
        await notifyClaimDetailsActions.selectDefendantSolicitor();
        await notifyClaimDetailsActions.uploadNotifyClaimDetails();
        await notifyClaimDetailsActions.submitNotifyClaimDetails();
      },
      async () => {
        await notifyClaimDetailsActions.confirmNotifyClaimDetails();
      },
      ccdEvents.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS,

      { verifySuccessEvent: false },
    );
  }

  async NotifyClaimDetails1v1LIP() {
    const { notifyClaimDetailsActions } = this.claimantSolicitorActionsFactory;
    await this.retryExuiEvent(
      async () => {
        await notifyClaimDetailsActions.certificateOfService1NotifyClaimDetails();
        await notifyClaimDetailsActions.submitNotifyClaimDetailsLIP();
      },
      async () => {
        await notifyClaimDetailsActions.confirmNotifyClaimDetailsCOS();
      },
      ccdEvents.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS,

      { verifySuccessEvent: false },
    );
  }

  async NotifyClaimDetails1v2LIPS() {
    const { notifyClaimDetailsActions } = this.claimantSolicitorActionsFactory;
    await this.retryExuiEvent(
      async () => {
        await notifyClaimDetailsActions.certificateOfService1NotifyClaimDetails();
        await notifyClaimDetailsActions.certificateOfService2NotifyClaimDetails();
        await notifyClaimDetailsActions.submitNotifyClaimDetailsCOS();
      },
      async () => {
        await notifyClaimDetailsActions.confirmNotifyClaimDetailsCOS();
      },
      ccdEvents.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS,

      { verifySuccessEvent: false },
    );
  }

  async NotifyClaimDetails1v2LIPLR() {
    const { notifyClaimDetailsActions } = this.claimantSolicitorActionsFactory;
    await this.retryExuiEvent(
      async () => {
        await notifyClaimDetailsActions.uploadNotifyClaimDetails();
        await notifyClaimDetailsActions.certificateOfService2NotifyClaimDetails();
        await notifyClaimDetailsActions.submitNotifyClaimDetailsLIPLR();
      },
      async () => {
        await notifyClaimDetailsActions.confirmNotifyClaimDetailsCOS();
      },
      ccdEvents.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS,

      { verifySuccessEvent: false },
    );
  }

  async RespondFastTrackIntentToProceed1v1() {
    const { claimantResponseActions } = this.claimantSolicitorActionsFactory;
    await this.retryExuiEvent(
      async () => {
        await claimantResponseActions.respondentResponse();
        await claimantResponseActions.defenceResponseDocument();
        await claimantResponseActions.dqFastTrack();
        await claimantResponseActions.statementOfTruth();
        await claimantResponseActions.submitClaimantResponse();
      },
      async () => {
        await claimantResponseActions.confirmClaimantResponse();
      },
      ccdEvents.CLAIMANT_RESPONSE,

      { verifySuccessEvent: false },
    );
  }

  async RespondSmallClaimIntentToProceed1v1() {
    const { claimantResponseActions } = this.claimantSolicitorActionsFactory;
    await this.retryExuiEvent(
      async () => {
        await claimantResponseActions.respondentResponse();
        await claimantResponseActions.defenceResponseDocument();
        await claimantResponseActions.dqSmallTrack();
        await claimantResponseActions.statementOfTruth();
        await claimantResponseActions.submitClaimantResponse();
      },
      async () => {
        await claimantResponseActions.confirmClaimantResponse();
      },
      ccdEvents.CLAIMANT_RESPONSE,

      { verifySuccessEvent: false },
    );
  }

  async RespondSmallClaimIntentToProceed2v1() {
    const { claimantResponseActions } = this.claimantSolicitorActionsFactory;
    await this.retryExuiEvent(
      async () => {
        await claimantResponseActions.respondentResponse2v1();
        await claimantResponseActions.defenceResponseDocument();
        await claimantResponseActions.dqSmallTrack();
        await claimantResponseActions.statementOfTruth();
        await claimantResponseActions.submitClaimantResponse();
      },
      async () => {
        await claimantResponseActions.confirmClaimantResponse();
      },
      ccdEvents.CLAIMANT_RESPONSE,

      { verifySuccessEvent: false },
    );
  }

  async RespondSmallClaimIntentToProceed1v2SS() {
    const { claimantResponseActions } = this.claimantSolicitorActionsFactory;
    await this.retryExuiEvent(
      async () => {
        await claimantResponseActions.respondentResponse1v2SS();
        await claimantResponseActions.defenceResponseDocument1v2SS();
        await claimantResponseActions.dqSmallTrack();
        await claimantResponseActions.statementOfTruth();
        await claimantResponseActions.submitClaimantResponse();
      },
      async () => {
        await claimantResponseActions.confirmClaimantResponse();
      },
      ccdEvents.CLAIMANT_RESPONSE,

      { verifySuccessEvent: false },
    );
  }

  async RespondSmallClaimIntentToProceed1v2DS() {
    const { claimantResponseActions } = this.claimantSolicitorActionsFactory;
    await this.retryExuiEvent(
      async () => {
        await claimantResponseActions.respondentResponse1v2DS();
        await claimantResponseActions.defenceResponseDocument1v2DS();
        await claimantResponseActions.dqSmallTrack();
        await claimantResponseActions.statementOfTruth();
        await claimantResponseActions.submitClaimantResponse();
      },
      async () => {
        await claimantResponseActions.confirmClaimantResponse();
      },
      ccdEvents.CLAIMANT_RESPONSE,

      { verifySuccessEvent: false },
    );
  }

  async RequestDefaultJudgment() {
    const { defaultJudgementActions } = this.claimantSolicitorActionsFactory;
    await this.retryExuiEvent(
      async () => {
        await defaultJudgementActions.defendantDetails();
        await defaultJudgementActions.showCertifyStatement();
        await defaultJudgementActions.hearingType();
        await defaultJudgementActions.hearingSupportRequirementsFieldDJ();
        await defaultJudgementActions.submitDefaultJudgment();
      },
      async () => {
        await defaultJudgementActions.confirmDefaultJudgment();
      },
      ccdEvents.DEFAULT_JUDGEMENT,

      { verifySuccessEvent: false },
    );
  }

  async RequestDefaultJudgment1v2() {
    const { defaultJudgementActions } = this.claimantSolicitorActionsFactory;
    await this.retryExuiEvent(
      async () => {
        await defaultJudgementActions.defendantDetails1v2();
        await defaultJudgementActions.showCertifyStatement();
        await defaultJudgementActions.hearingType();
        await defaultJudgementActions.hearingSupportRequirementsFieldDJ();
        await defaultJudgementActions.submitDefaultJudgment();
      },
      async () => {
        await defaultJudgementActions.confirmDefaultJudgment();
      },
      ccdEvents.DEFAULT_JUDGEMENT,

      { verifySuccessEvent: false },
    );
  }
}
