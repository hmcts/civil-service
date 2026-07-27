import BaseApi from '../../../base/base-api';
import { defendantSolicitor2User } from '../../../config/users/exui-users';
import { AllMethodsStep } from '../../../decorators/test-steps';
import CaseRole from '../../../constants/cases/case-role';
import CaseState from '../../../constants/cases/case-state';
import ccdEvents from '../../../constants/ccd-events/ccd-events';
import ClaimantDefendantSolicitorDataBuilderFactory from '../../../data-builders/exui/claimant-defendant-solicitor/claimant-defendant-solicitor-data-builder-factory';
import UserAssignedCasesHelper from '../../../helpers/user-assigned-cases-helper';
import ZodHelper from '../../../helpers/zod-helper';
import ClaimantDefendantSolicitorSchemaBuilderFactory from '../../../schema-builders/exui/claimant-defendant-solicitor/claimant-defendant-solicitor-schema-builder-factory';
import TestData from '../../../models/test-utils/test-data';
import RequestsFactory from '../../../requests/requests-factory';

@AllMethodsStep()
export default class DefendantSolicitor2ApiSteps extends BaseApi {
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

  async AssignCaseRole() {
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

  async AcknowledgeClaimFullDefence() {
    await this.setupApiStep(defendantSolicitor2User);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { acknowledgeClaimDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const acknowledgeClaimData = await acknowledgeClaimDataBuilder.buildDataDS2FullDefence();
    await super.submitCCDEvent(
      defendantSolicitor2User,
      ccdEvents.ACKNOWLEDGE_CLAIM,
      acknowledgeClaimData,
      CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT,
    );

    const { acknowledgeClaimSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const acknowledgeClaimSchema =
      await acknowledgeClaimSchemaBuilder.buildSchemaDS2FullDefence(caseDataBeforeSubmission);
    ZodHelper.safeParse(acknowledgeClaimSchema, this.ccdCaseData);
  }

  async InformAgreedExtensionDate() {
    await this.setupApiStep(defendantSolicitor2User);
    await super.fetchAndSetCCDCaseData();
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { informAgreedExtensionDateDataBuilder } =
      this.claimantDefendantSolicitorDataBuilderFactory;
    const informAgreedExtensionDateEventData =
      await informAgreedExtensionDateDataBuilder.buildDataDS2();

    await super.submitCCDEvent(
      defendantSolicitor2User,
      ccdEvents.INFORM_AGREED_EXTENSION_DATE,
      informAgreedExtensionDateEventData,
      CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT,
    );

    const { informAgreedExtensionDateSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const informAgreedExtensionDateSchema =
      await informAgreedExtensionDateSchemaBuilder.buildSchemaDS2(caseDataBeforeSubmission);
    ZodHelper.safeParse(informAgreedExtensionDateSchema, this.ccdCaseData);
  }

  async RespondFastFullDefence() {
    await this.setupApiStep(defendantSolicitor2User);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { defendantResponseDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData = await defendantResponseDataBuilder.buildDS2FastFullDefence();

    await super.submitCCDEvent(
      defendantSolicitor2User,
      ccdEvents.DEFENDANT_RESPONSE,
      defendantResponseEventData,
      CaseState.AWAITING_APPLICANT_INTENTION,
    );

    const { defendantResponseSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSchemaBuilder.buildDS2FastFullDefence(caseDataBeforeSubmission);
    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondFastTrackFullDefence1v2DS() {
    await this.setupApiStep(defendantSolicitor2User);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { defendantResponseDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseDataBuilder.buildDS2FastTrackFullDefence1v2DS();

    await super.submitCCDEvent(
      defendantSolicitor2User,
      ccdEvents.DEFENDANT_RESPONSE,
      defendantResponseEventData,
      CaseState.AWAITING_APPLICANT_INTENTION,
    );

    const { defendantResponseSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSchemaBuilder.buildDS2FastTrackFullDefence1v2DS(
        caseDataBeforeSubmission,
      );
    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }

  async RespondMultiFullDefence() {
    await this.setupApiStep(defendantSolicitor2User);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { defendantResponseDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defendantResponseEventData =
      await defendantResponseDataBuilder.buildDS2MultiFullDefence1v2DS();

    await super.submitCCDEvent(
      defendantSolicitor2User,
      ccdEvents.DEFENDANT_RESPONSE,
      defendantResponseEventData,
      CaseState.AWAITING_APPLICANT_INTENTION,
    );

    const { defendantResponseSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defendantResponseSchema =
      await defendantResponseSchemaBuilder.buildDS2MultiFullDefence1v2DS(caseDataBeforeSubmission);
    ZodHelper.safeParse(defendantResponseSchema, this.ccdCaseData);
  }
}
