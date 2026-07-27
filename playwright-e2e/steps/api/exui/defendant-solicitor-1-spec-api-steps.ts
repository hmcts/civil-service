import BaseApi from '../../../base/base-api';
import { defendantSolicitor1User } from '../../../config/users/exui-users';
import ccdEvents from '../../../constants/ccd-events/ccd-events';
import ClaimantDefendantSolicitorDataBuilderFactory from '../../../data-builders/exui/claimant-defendant-solicitor/claimant-defendant-solicitor-data-builder-factory';
import { AllMethodsStep } from '../../../decorators/test-steps';
import CaseRole from '../../../constants/cases/case-role';
import CaseState from '../../../constants/cases/case-state';
import UserAssignedCasesHelper from '../../../helpers/user-assigned-cases-helper';
import ZodHelper from '../../../helpers/zod-helper';
import TestData from '../../../models/test-utils/test-data';
import RequestsFactory from '../../../requests/requests-factory';
import ClaimantDefendantSolicitorSchemaBuilderFactory from '../../../schema-builders/exui/claimant-defendant-solicitor/claimant-defendant-solicitor-schema-builder-factory';

@AllMethodsStep()
export default class DefendantSolicitor1SpecApiSteps extends BaseApi {
  private claimantDefendantSolicitorDataBuilderFactory: ClaimantDefendantSolicitorDataBuilderFactory;
  private claimantDefendantSolicitorSchemaBuilderFactory: ClaimantDefendantSolicitorSchemaBuilderFactory;

  constructor(
    claimantDefendantSolicitorDataBuilderFactory: ClaimantDefendantSolicitorDataBuilderFactory,
    claimantDefendantSolicitorSchemaBuilderFactory: ClaimantDefendantSolicitorSchemaBuilderFactory,
    requestsFactory: RequestsFactory,
    testData: TestData,
  ) {
    super(requestsFactory, testData);
    this.claimantDefendantSolicitorDataBuilderFactory =
      claimantDefendantSolicitorDataBuilderFactory;
    this.claimantDefendantSolicitorSchemaBuilderFactory =
      claimantDefendantSolicitorSchemaBuilderFactory;
  }

  async AssignCaseRoleToDefendant1() {
    await this.setupApiStep(defendantSolicitor1User);
    const { civilServiceRequests } = this.requestsFactory;
    await civilServiceRequests.assignCaseToDefendant(
      defendantSolicitor1User,
      CaseRole.RESPONDENT_SOLICITOR_ONE,
      this.ccdCaseData?.id,
    );
    await super.fetchAndSetCCDCaseData();
    UserAssignedCasesHelper.addAssignedCaseToUser(defendantSolicitor1User, this.ccdCaseData?.id);
  }

  async InformAgreedExtensionDateSpec() {
    await this.setupApiStep(defendantSolicitor1User);
    await super.fetchAndSetCCDCaseData();
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { informAgreedExtensionDateSpecDataBuilder } =
      this.claimantDefendantSolicitorDataBuilderFactory;
    const informAgreedExtensionDateEventData =
      await informAgreedExtensionDateSpecDataBuilder.buildDataDS1();

    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.INFORM_AGREED_EXTENSION_DATE_SPEC,
      informAgreedExtensionDateEventData,
      CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT,
    );

    const { informAgreedExtensionDateSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const informAgreedExtensionDateSchema =
      await informAgreedExtensionDateSpecSchemaBuilder.buildDataDS1(caseDataBeforeSubmission);
    ZodHelper.safeParse(informAgreedExtensionDateSchema, this.ccdCaseData);
  }

  async RespondFastFullDefence() {
    await this.setupApiStep(defendantSolicitor1User);

    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);
    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS1FastFullDefence();

    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
      CaseState.AWAITING_APPLICANT_INTENTION,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildFastFullDefence(caseDataBeforeSubmission);

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondIntermediateFullDefence() {
    await this.setupApiStep(defendantSolicitor1User);

    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);
    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS1IntermediateFullDefence();

    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildIntermediateFullDefence(
        caseDataBeforeSubmission,
      );

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondMultiFullDefence() {
    await this.setupApiStep(defendantSolicitor1User);

    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);
    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS1MultiFullDefence();

    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildMultiFullDefence(caseDataBeforeSubmission);

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondMultiFullDefence1v2SS() {
    await this.setupApiStep(defendantSolicitor1User);

    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);
    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS1MultiFullDefence1v2SS();

    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
      CaseState.AWAITING_APPLICANT_INTENTION,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildMultiFullDefence1v2SS(caseDataBeforeSubmission);

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }


  async RespondSmallFullDefence() {
    await this.setupApiStep(defendantSolicitor1User);

    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);
    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS1SmallFullDefence();

    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
      CaseState.AWAITING_APPLICANT_INTENTION,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildSmallFullDefence(caseDataBeforeSubmission);

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondFastPartAdmitImmediately() {
    await this.setupApiStep(defendantSolicitor1User);

    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);
    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS1FastPartAdmitImmediately();

    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
      CaseState.AWAITING_APPLICANT_INTENTION,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildDS1FastPartAdmitImmediately(
        caseDataBeforeSubmission,
      );

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondIntermediatePartAdmitImmediately() {
    await this.setupApiStep(defendantSolicitor1User);

    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);
    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS1IntermediatePartAdmitImmediately();

    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
      CaseState.AWAITING_APPLICANT_INTENTION,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildDS1IntermediatePartAdmitImmediately(
        caseDataBeforeSubmission,
      );

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondMultiPartAdmitImmediately() {
    await this.setupApiStep(defendantSolicitor1User);

    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);
    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS1MultiPartAdmitImmediately();

    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
      CaseState.AWAITING_APPLICANT_INTENTION,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildDS1MultiPartAdmitImmediately(
        caseDataBeforeSubmission,
      );

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondFullAdmitImmediately() {
    await this.setupApiStep(defendantSolicitor1User);

    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);
    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS1FullAdmitImmediately();

    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
      CaseState.AWAITING_APPLICANT_INTENTION,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildDS1FullAdmitImmediately(
        caseDataBeforeSubmission,
      );

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondMultiFullAdmitImmediately() {
    await this.setupApiStep(defendantSolicitor1User);

    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);
    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS1MultiFullAdmitImmediately();

    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
      CaseState.AWAITING_APPLICANT_INTENTION,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildDS1MultiFullAdmitImmediately(
        caseDataBeforeSubmission,
      );

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondFullAdmitSetDate1v2SS() {
    await this.setupApiStep(defendantSolicitor1User);

    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);
    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS1FullAdmitSetDate1v2SS();

    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
      CaseState.AWAITING_APPLICANT_INTENTION,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildDS1FullAdmitSetDate1v2SS(
        caseDataBeforeSubmission,
      );

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondFullAdmitRepayment2v1() {
    await this.setupApiStep(defendantSolicitor1User);

    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);
    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS1FullAdmitRepayment2v1();

    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
      CaseState.AWAITING_APPLICANT_INTENTION,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildDS1FullAdmitRepayment2v1(
        caseDataBeforeSubmission,
      );

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondSmallPartAdmitImmediately() {
    await this.setupApiStep(defendantSolicitor1User);

    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);
    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS1SmallPartAdmitImmediately();

    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
      CaseState.AWAITING_APPLICANT_INTENTION,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildDS1SmallPartAdmitImmediately(
        caseDataBeforeSubmission,
      );

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondSmallPartAdmitHasPaid() {
    await this.setupApiStep(defendantSolicitor1User);

    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);
    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS1SmallPartAdmitHasPaid();

    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
      CaseState.AWAITING_APPLICANT_INTENTION,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildDS1SmallPartAdmitHasPaid(
        caseDataBeforeSubmission,
      );

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondFastPartAdmitSetDate1v2SS() {
    await this.setupApiStep(defendantSolicitor1User);

    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);
    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS1FastPartAdmitSetDate1v2SS();

    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
      CaseState.AWAITING_APPLICANT_INTENTION,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildDS1FastPartAdmitSetDate1v2SS(
        caseDataBeforeSubmission,
      );

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondSmallPartAdmitSetDate1v2SS() {
    await this.setupApiStep(defendantSolicitor1User);

    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);
    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS1SmallPartAdmitSetDate1v2SS();

    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
      CaseState.AWAITING_APPLICANT_INTENTION,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildDS1SmallPartAdmitSetDate1v2SS(
        caseDataBeforeSubmission,
      );

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondFastPartAdmitRepayment2v1() {
    await this.setupApiStep(defendantSolicitor1User);

    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);
    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS1FastPartAdmitRepayment2v1();

    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
      CaseState.AWAITING_APPLICANT_INTENTION,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildDS1FastPartAdmitRepayment2v1(
        caseDataBeforeSubmission,
      );

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondSmallPartAdmitRepayment2v1() {
    await this.setupApiStep(defendantSolicitor1User);

    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);
    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS1SmallPartAdmitRepayment2v1();

    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
      CaseState.AWAITING_APPLICANT_INTENTION,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildDS1SmallPartAdmitRepayment2v1(
        caseDataBeforeSubmission,
      );

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondCounterClaim() {
    await this.setupApiStep(defendantSolicitor1User);

    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);
    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS1CounterClaim();

    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildDS1CounterClaim(caseDataBeforeSubmission);

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondMultiCounterClaim() {
    await this.setupApiStep(defendantSolicitor1User);

    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);
    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS1MultiCounterClaim();

    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
      CaseState.AWAITING_APPLICANT_INTENTION,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildDS1MultiCounterClaim(caseDataBeforeSubmission);

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondCounterClaim2v1() {
    await this.setupApiStep(defendantSolicitor1User);

    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);
    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS1CounterClaim2v1();

    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildDS1CounterClaim2v1(caseDataBeforeSubmission);

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondCounterClaim1v2SS() {
    await this.setupApiStep(defendantSolicitor1User);

    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);
    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS1CounterClaim1v2SS();

    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildDS1CounterClaim1v2SS(caseDataBeforeSubmission);

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondFastFullDefence2v1() {
    await this.setupApiStep(defendantSolicitor1User);

    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);
    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS1Fast2v1FullDefence();

    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
      CaseState.AWAITING_APPLICANT_INTENTION,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildFast2v1FullDefence(caseDataBeforeSubmission);

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondSmallFullDefence2v1() {
    await this.setupApiStep(defendantSolicitor1User);

    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);
    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS1Small2v1FullDefence();

    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
      CaseState.AWAITING_APPLICANT_INTENTION,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildSmall2v1FullDefence(
        caseDataBeforeSubmission,
      );

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondFastFullDefence1v2SS() {
    await this.setupApiStep(defendantSolicitor1User);

    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);
    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS1FastFullDefence1v2SS();

    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
      CaseState.AWAITING_APPLICANT_INTENTION,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildFast1v2SSFullDefence(caseDataBeforeSubmission);

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondIntermediateFullDefence1v2SS() {
    await this.setupApiStep(defendantSolicitor1User);

    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);
    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS1IntermediateFullDefence1v2SS();

    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
      CaseState.AWAITING_APPLICANT_INTENTION,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildIntermediate1v2SSFullDefence(
        caseDataBeforeSubmission,
      );

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondSmallFullDefence1v2SS() {
    await this.setupApiStep(defendantSolicitor1User);

    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);
    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS1SmallFullDefence1v2SS();

    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
      CaseState.AWAITING_APPLICANT_INTENTION,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildSmall1v2SSFullDefence(caseDataBeforeSubmission);
    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondFastFullDefence1v2DS() {
    await this.setupApiStep(defendantSolicitor1User);

    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);
    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS1FastFullDefence();

    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
      CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildFastFullDefence(caseDataBeforeSubmission);

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondSmallFullDefence1v2DS() {
    await this.setupApiStep(defendantSolicitor1User);

    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);
    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS1SmallFullDefence();

    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
      CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildSmallFullDefence(caseDataBeforeSubmission);

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async EvidenceUploadFast() {
    await this.setupApiStep(defendantSolicitor1User);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { evidenceUploadRespondentDataBuilder } =
      this.claimantDefendantSolicitorDataBuilderFactory;
    const evidenceUploadRespondentData = await evidenceUploadRespondentDataBuilder.buildDS1Fast();
    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.EVIDENCE_UPLOAD_RESPONDENT,
      evidenceUploadRespondentData,
      CaseState.CASE_PROGRESSION,
    );

    const { evidenceUploadRespondentSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const evidenceUploadRespondentSchema =
      await evidenceUploadRespondentSchemaBuilder.buildDS1Fast(caseDataBeforeSubmission);
    ZodHelper.safeParse(evidenceUploadRespondentSchema, this.ccdCaseData);
  }

  async EvidenceUploadSmall() {
    await this.setupApiStep(defendantSolicitor1User);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { evidenceUploadRespondentDataBuilder } =
      this.claimantDefendantSolicitorDataBuilderFactory;
    const evidenceUploadRespondentData =
      await evidenceUploadRespondentDataBuilder.buildDS1Small();
    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.EVIDENCE_UPLOAD_RESPONDENT,
      evidenceUploadRespondentData,
      CaseState.CASE_PROGRESSION,
    );

    const { evidenceUploadRespondentSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const evidenceUploadRespondentSchema =
      await evidenceUploadRespondentSchemaBuilder.buildDS1SmallClaim(caseDataBeforeSubmission);
    ZodHelper.safeParse(evidenceUploadRespondentSchema, this.ccdCaseData);
  }

  async UploadMediationDocuments() {
    await this.setupApiStep(defendantSolicitor1User);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { uploadMediationDocumentsDataBuilder } =
      this.claimantDefendantSolicitorDataBuilderFactory;
    const uploadMediationDocumentsData = await uploadMediationDocumentsDataBuilder.buildDS1();
    await super.submitCCDEvent(
      defendantSolicitor1User,
      ccdEvents.UPLOAD_MEDIATION_DOCUMENTS,
      uploadMediationDocumentsData,
    );

    const { uploadMediationDocumentsSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const uploadMediationDocumentsSchema =
      await uploadMediationDocumentsSchemaBuilder.buildDS1(caseDataBeforeSubmission);
    ZodHelper.safeParse(uploadMediationDocumentsSchema, this.ccdCaseData);
  }
}
