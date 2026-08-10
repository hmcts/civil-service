import BaseApi from '../../../base/base-api';
import { defendantSolicitor2User } from '../../../config/users/exui-users';
import ccdEvents from '../../../constants/ccd-events/ccd-events';
import { AllMethodsStep } from '../../../decorators/test-steps';
import CaseRole from '../../../constants/cases/case-role';
import CaseState from '../../../constants/cases/case-state';
import ClaimantDefendantSolicitorDataBuilderFactory from '../../../data-builders/exui/claimant-defendant-solicitor/claimant-defendant-solicitor-data-builder-factory';
import UserAssignedCasesHelper from '../../../helpers/user-assigned-cases-helper';
import ZodHelper from '../../../helpers/zod-helper';
import ClaimantDefendantSolicitorSchemaBuilderFactory from '../../../schema-builders/exui/claimant-defendant-solicitor/claimant-defendant-solicitor-schema-builder-factory';
import TestData from '../../../models/test-utils/test-data';
import RequestsFactory from '../../../requests/requests-factory';

@AllMethodsStep()
export default class DefendantSolicitor2SpecApiSteps extends BaseApi {
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

  async AssignCaseRoleToDefendant2() {
    await this.setupApiStep(defendantSolicitor2User);
    const { civilServiceRequests } = this.requestsFactory;
    await civilServiceRequests.assignCaseToDefendant(
      defendantSolicitor2User,
      CaseRole.RESPONDENT_SOLICITOR_TWO,
      this.ccdCaseData.id!,
    );
    await super.fetchAndSetCCDCaseData();
    UserAssignedCasesHelper.addAssignedCaseToUser(defendantSolicitor2User, this.ccdCaseData.id!);
  }

  async RespondFastFullDefence() {
    await this.setupApiStep(defendantSolicitor2User);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS2FastFullDefence();

    await super.submitCCDEvent(
      defendantSolicitor2User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
      CaseState.AWAITING_APPLICANT_INTENTION,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildDS2FastFullDefence(caseDataBeforeSubmission);

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondIntermediateFullDefence() {
    await this.setupApiStep(defendantSolicitor2User);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS2IntermediateFullDefence();

    await super.submitCCDEvent(
      defendantSolicitor2User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
      CaseState.AWAITING_APPLICANT_INTENTION,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildDS2IntermediateFullDefence(
        caseDataBeforeSubmission,
      );

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondMultiFullDefence() {
    await this.setupApiStep(defendantSolicitor2User);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS2MultiFullDefence1v2DS();

    await super.submitCCDEvent(
      defendantSolicitor2User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
      CaseState.AWAITING_APPLICANT_INTENTION,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildDS2MultiFullDefence1v2DS(
        caseDataBeforeSubmission,
      );

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondSmallFullDefence() {
    await this.setupApiStep(defendantSolicitor2User);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { defendantResponseSpecDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseSpecDataBuilder.buildDS2SmallFullDefence();

    await super.submitCCDEvent(
      defendantSolicitor2User,
      ccdEvents.DEFENDANT_RESPONSE_SPEC,
      defendantResponseEventData,
      CaseState.AWAITING_APPLICANT_INTENTION,
    );

    const { defendantResponseSpecSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSpecSchemaBuilder.buildDS2Small1v2DSFullDefence(
        caseDataBeforeSubmission,
      );

    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async EvidenceUploadFast1v2SS() {
    await this.setupApiStep(defendantSolicitor2User);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { evidenceUploadRespondentDataBuilder } =
      this.claimantDefendantSolicitorDataBuilderFactory;
    const evidenceUploadRespondentData =
      await evidenceUploadRespondentDataBuilder.buildDS2Fast1v2SS();
    await super.submitCCDEvent(
      defendantSolicitor2User,
      ccdEvents.EVIDENCE_UPLOAD_RESPONDENT,
      evidenceUploadRespondentData,
      CaseState.CASE_PROGRESSION,
    );

    const { evidenceUploadRespondentSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const evidenceUploadRespondentSchema =
      await evidenceUploadRespondentSchemaBuilder.buildDS2Fast1v2SS(caseDataBeforeSubmission);
    ZodHelper.safeParse(evidenceUploadRespondentSchema, this.ccdCaseData);
  }

  async UploadMediationDocuments() {
    await this.setupApiStep(defendantSolicitor2User);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { uploadMediationDocumentsDataBuilder } =
      this.claimantDefendantSolicitorDataBuilderFactory;
    const uploadMediationDocumentsData = await uploadMediationDocumentsDataBuilder.buildDS2();
    await super.submitCCDEvent(
      defendantSolicitor2User,
      ccdEvents.UPLOAD_MEDIATION_DOCUMENTS,
      uploadMediationDocumentsData,
    );

    const { uploadMediationDocumentsSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const uploadMediationDocumentsSchema =
      await uploadMediationDocumentsSchemaBuilder.buildDS2(caseDataBeforeSubmission);
    ZodHelper.safeParse(uploadMediationDocumentsSchema, this.ccdCaseData);
  }
}
