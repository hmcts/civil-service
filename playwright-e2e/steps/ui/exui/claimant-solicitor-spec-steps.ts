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
export default class ClaimantSolicitorSpecSteps extends BaseExui {
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
    const { createClaimSpecActions } = this.claimantSolicitorActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await createClaimSpecActions.checklist();
        await createClaimSpecActions.eligibiltySpec();
        await createClaimSpecActions.references();
        await createClaimSpecActions.claimant();
        await createClaimSpecActions.noAddAnotherClaimant();
        await createClaimSpecActions.claimantDetails();
        await createClaimSpecActions.defendant();
        await createClaimSpecActions.defendantDetails();
        await createClaimSpecActions.noAddAnotherDefendant();
        await createClaimSpecActions.claimDetailsFastTrack();
        await createClaimSpecActions.statementOfTruthCreateClaim();
        await createClaimSpecActions.submitCreateClaim();
      },
      async () => {
        await createClaimSpecActions.confirmCreateClaimSpec();
      },
      ccdEvents.CREATE_CLAIM_SPEC,

      { verifySuccessEvent: false },
    );
  }

  async CreateClaimSmallTrack1v1() {
    const { createClaimSpecActions } = this.claimantSolicitorActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await createClaimSpecActions.checklist();
        await createClaimSpecActions.eligibiltySpec();
        await createClaimSpecActions.references();
        await createClaimSpecActions.claimant();
        await createClaimSpecActions.noAddAnotherClaimant();
        await createClaimSpecActions.claimantDetails();
        await createClaimSpecActions.defendant();
        await createClaimSpecActions.defendantDetails();
        await createClaimSpecActions.noAddAnotherDefendant();
        await createClaimSpecActions.claimDetailsSmallTrack();
        await createClaimSpecActions.statementOfTruthCreateClaim();
        await createClaimSpecActions.submitCreateClaim();
      },
      async () => {
        await createClaimSpecActions.confirmCreateClaimSpec();
      },
      ccdEvents.CREATE_CLAIM_SPEC,
      { verifySuccessEvent: false },
    );
  }

  async CreateClaimSmallTrack2v1() {
    const { createClaimSpecActions } = this.claimantSolicitorActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await createClaimSpecActions.checklist();
        await createClaimSpecActions.eligibiltySpec();
        await createClaimSpecActions.references();
        await createClaimSpecActions.claimant();
        await createClaimSpecActions.addAnotherClaimant();
        await createClaimSpecActions.secondClaimant();
        await createClaimSpecActions.claimantDetails();
        await createClaimSpecActions.defendant();
        await createClaimSpecActions.defendantDetails();
        await createClaimSpecActions.claimDetailsSmallTrack();
        await createClaimSpecActions.statementOfTruthCreateClaim();
        await createClaimSpecActions.submitCreateClaim();
      },
      async () => {
        await createClaimSpecActions.confirmCreateClaimSpec();
      },
      ccdEvents.CREATE_CLAIM_SPEC,
      { verifySuccessEvent: false },
    );
  }

  async CreateClaimSmallTrack1v2SS() {
    const { createClaimSpecActions } = this.claimantSolicitorActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await createClaimSpecActions.checklist();
        await createClaimSpecActions.eligibiltySpec();
        await createClaimSpecActions.references();
        await createClaimSpecActions.claimant();
        await createClaimSpecActions.noAddAnotherClaimant();
        await createClaimSpecActions.claimantDetails();
        await createClaimSpecActions.defendant();
        await createClaimSpecActions.defendantDetails();
        await createClaimSpecActions.addAnotherDefendant();
        await createClaimSpecActions.secondDefendant();
        await createClaimSpecActions.secondDefedantSSDetails();
        await createClaimSpecActions.claimDetailsSmallTrack();
        await createClaimSpecActions.statementOfTruthCreateClaim();
        await createClaimSpecActions.submitCreateClaim();
      },
      async () => {
        await createClaimSpecActions.confirmCreateClaimSpec();
      },
      ccdEvents.CREATE_CLAIM_SPEC,
      { verifySuccessEvent: false },
    );
  }

  async CreateClaimSmallTrack1v2DS() {
    const { createClaimSpecActions } = this.claimantSolicitorActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await createClaimSpecActions.checklist();
        await createClaimSpecActions.eligibiltySpec();
        await createClaimSpecActions.references();
        await createClaimSpecActions.claimant();
        await createClaimSpecActions.noAddAnotherClaimant();
        await createClaimSpecActions.claimantDetails();
        await createClaimSpecActions.defendant();
        await createClaimSpecActions.defendantDetails();
        await createClaimSpecActions.addAnotherDefendant();
        await createClaimSpecActions.secondDefendant();
        await createClaimSpecActions.secondDefendantDSDetails();
        await createClaimSpecActions.claimDetailsSmallTrack();
        await createClaimSpecActions.statementOfTruthCreateClaim();
        await createClaimSpecActions.submitCreateClaim();
      },
      async () => {
        await createClaimSpecActions.confirmCreateClaimSpec();
      },
      ccdEvents.CREATE_CLAIM_SPEC,
      { verifySuccessEvent: false },
    );
  }

  async RespondFastTrackIntentToProceed1v1() {
    const { claimantResponseSpecActions } = this.claimantSolicitorActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await claimantResponseSpecActions.respondentResponseSpec();
        await claimantResponseSpecActions.defenceDocumentSpec();
        await claimantResponseSpecActions.dqFastTrackClaimantResponseSpec();
        await claimantResponseSpecActions.dqFastTrack();
        await claimantResponseSpecActions.application();
        await claimantResponseSpecActions.statementOfTruthClaimantResponse();
        await claimantResponseSpecActions.submitClaimantResponse();
      },
      async () => {
        await claimantResponseSpecActions.confirm();
      },
      ccdEvents.CLAIMANT_RESPONSE_SPEC,
      { verifySuccessEvent: false },
    );
  }

  async RespondSmallClaimIntentToProceed1v1() {
    const { claimantResponseSpecActions } = this.claimantSolicitorActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await claimantResponseSpecActions.respondentResponseSpec();
        await claimantResponseSpecActions.defenceDocumentSpec();
        await claimantResponseSpecActions.mediationClaimantResponseSpec();
        await claimantResponseSpecActions.smallClaimExperts();
        await claimantResponseSpecActions.dqSmallTrack();
        await claimantResponseSpecActions.statementOfTruthClaimantResponse();
        await claimantResponseSpecActions.submitClaimantResponse();
      },
      async () => {
        await claimantResponseSpecActions.confirm();
      },
      ccdEvents.CLAIMANT_RESPONSE_SPEC,
      { verifySuccessEvent: false },
    );
  }

  async RespondSmallClaimIntentToProceed2v1() {
    const { claimantResponseSpecActions } = this.claimantSolicitorActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await claimantResponseSpecActions.respondentResponse2v1Spec();
        await claimantResponseSpecActions.defenceDocumentSpec();
        await claimantResponseSpecActions.mediationClaimantResponseSpec();
        await claimantResponseSpecActions.smallClaimExperts2v1();
        await claimantResponseSpecActions.dqSmallTrack();
        await claimantResponseSpecActions.statementOfTruthClaimantResponse();
        await claimantResponseSpecActions.submitClaimantResponse();
      },
      async () => {
        await claimantResponseSpecActions.confirm();
      },
      ccdEvents.CLAIMANT_RESPONSE_SPEC,
      { verifySuccessEvent: false },
    );
  }

  async RespondSmallClaimIntentToProceed1v2SS() {
    const { claimantResponseSpecActions } = this.claimantSolicitorActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await claimantResponseSpecActions.respondentResponse1v2SSSpec();
        await claimantResponseSpecActions.defenceDocumentSpec();
        await claimantResponseSpecActions.mediationClaimantResponseSpec();
        await claimantResponseSpecActions.smallClaimExperts();
        await claimantResponseSpecActions.dqSmallTrack();
        await claimantResponseSpecActions.statementOfTruthClaimantResponse();
        await claimantResponseSpecActions.submitClaimantResponse();
      },
      async () => {
        await claimantResponseSpecActions.confirm();
      },
      ccdEvents.CLAIMANT_RESPONSE_SPEC,
      { verifySuccessEvent: false },
    );
  }

  async RespondSmallClaimIntentToProceed1v2DS() {
    const { claimantResponseSpecActions } = this.claimantSolicitorActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await claimantResponseSpecActions.respondentResponse1v2DSSpec();
        await claimantResponseSpecActions.defenceDocumentSpec();
        await claimantResponseSpecActions.mediationClaimantResponseSpec();
        await claimantResponseSpecActions.smallClaimExperts();
        await claimantResponseSpecActions.dqSmallTrack();
        await claimantResponseSpecActions.statementOfTruthClaimantResponse();
        await claimantResponseSpecActions.submitClaimantResponse();
      },
      async () => {
        await claimantResponseSpecActions.confirm();
      },
      ccdEvents.CLAIMANT_RESPONSE_SPEC,
      { verifySuccessEvent: false },
    );
  }

  async RequestDefaultJudgment() {
    const { defaultJudgementSpecActions } = this.claimantSolicitorActionsFactory;
    await this.retryExuiEvent(
      async () => {
        await defaultJudgementSpecActions.defendantDetails();
        await defaultJudgementSpecActions.showCertifyStatement();
        await defaultJudgementSpecActions.claimPartialPayment();
        await defaultJudgementSpecActions.fixedCostsOnEntry();
        await defaultJudgementSpecActions.paymentBySetDate();
        await defaultJudgementSpecActions.submitDefaultJudgment();
      },
      async () => {
        await defaultJudgementSpecActions.confirmDefaultJudgmentSpec();
      },
      ccdEvents.DEFAULT_JUDGEMENT_SPEC,
      { verifySuccessEvent: false },
    );
  }

  async RequestDefaultJudgment1v2() {
    const { defaultJudgementSpecActions } = this.claimantSolicitorActionsFactory;
    await this.retryExuiEvent(
      async () => {
        await defaultJudgementSpecActions.defendantDetails1v2();
        await defaultJudgementSpecActions.showCertifyStatementMultipleDefendants();
        await defaultJudgementSpecActions.claimPartialPayment1v2();
        await defaultJudgementSpecActions.fixedCostsOnEntry();
        await defaultJudgementSpecActions.paymentWithRepayment1v2();
        await defaultJudgementSpecActions.submitDefaultJudgment();
      },
      async () => {
        await defaultJudgementSpecActions.confirmDefaultJudgmentSpec();
      },
      ccdEvents.DEFAULT_JUDGEMENT_SPEC,
      { verifySuccessEvent: false },
    );
  }
}
