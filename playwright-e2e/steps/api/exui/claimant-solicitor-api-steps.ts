import BaseApi from '../../../base/base-api';
import {
  claimantOrganisationSuperUser,
  claimantSolicitorUser,
} from '../../../config/users/exui-users';
import ccdEvents from '../../../constants/ccd-events/ccd-events';
import ClaimantDefendantSolicitorDataBuilderFactory from '../../../data-builders/exui/claimant-defendant-solicitor/claimant-defendant-solicitor-data-builder-factory';
import { AllMethodsStep } from '../../../decorators/test-steps';
import CaseState from '../../../constants/cases/case-state';
import DateHelper from '../../../helpers/date-helper';
import UserAssignedCasesHelper from '../../../helpers/user-assigned-cases-helper';
import ZodHelper from '../../../helpers/zod-helper';
import TestData from '../../../models/test-utils/test-data';
import RequestsFactory from '../../../requests/requests-factory';
import ClaimantDefendantSolicitorSchemaBuilderFactory from '../../../schema-builders/exui/claimant-defendant-solicitor/claimant-defendant-solicitor-schema-builder-factory';

@AllMethodsStep()
export default class ClaimantSolicitorApiSteps extends BaseApi {
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

  async CreateClaimSmall1v1() {
    await this.setupUserData(claimantSolicitorUser);
    const { createClaimDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const createClaimData = await createClaimDataBuilder.buildSmall1v1();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CREATE_CLAIM,
      createClaimData,
      CaseState.PENDING_CASE_ISSUED,
    );

    const { createClaimSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const createClaimResponseSchema = await createClaimSchemaBuilder.buildSmall1v1();
    ZodHelper.safeParse(createClaimResponseSchema, this.ccdCaseData);
    UserAssignedCasesHelper.addAssignedCaseToUser(claimantSolicitorUser, this.ccdCaseData?.id);
  }

  async CreateClaimFast1v1() {
    await this.setupUserData(claimantSolicitorUser);
    const { createClaimDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const createClaimData = await createClaimDataBuilder.buildFast1v1();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CREATE_CLAIM,
      createClaimData,
      CaseState.PENDING_CASE_ISSUED,
    );

    const { createClaimSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const createClaimResponseSchema = await createClaimSchemaBuilder.buildFast1v1();
    ZodHelper.safeParse(createClaimResponseSchema, this.ccdCaseData);
    UserAssignedCasesHelper.addAssignedCaseToUser(claimantSolicitorUser, this.ccdCaseData?.id);
  }

  async CreateClaimFastNIHL1v1() {
    await this.setupUserData(claimantSolicitorUser);
    const { createClaimDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const createClaimData = await createClaimDataBuilder.buildFastNIHL1v1();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CREATE_CLAIM,
      createClaimData,
      CaseState.PENDING_CASE_ISSUED,
    );

    const { createClaimSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const createClaimResponseSchema = await createClaimSchemaBuilder.buildFastNIHL1v1();
    ZodHelper.safeParse(createClaimResponseSchema, this.ccdCaseData);
    UserAssignedCasesHelper.addAssignedCaseToUser(claimantSolicitorUser, this.ccdCaseData?.id);
  }

  async CreateClaimFast1v2DS() {
    await this.setupUserData(claimantSolicitorUser);
    const { createClaimDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const createClaimData = await createClaimDataBuilder.buildFast1v2DS();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CREATE_CLAIM,
      createClaimData,
      CaseState.PENDING_CASE_ISSUED,
    );

    const { createClaimSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const createClaimResponseSchema = await createClaimSchemaBuilder.buildFast1v2DS();
    ZodHelper.safeParse(createClaimResponseSchema, this.ccdCaseData);
    UserAssignedCasesHelper.addAssignedCaseToUser(claimantSolicitorUser, this.ccdCaseData?.id);
  }

  async CreateClaimFast1v2SS() {
    await this.setupUserData(claimantSolicitorUser);
    const { createClaimDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const createClaimData = await createClaimDataBuilder.buildFast1v2SS();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CREATE_CLAIM,
      createClaimData,
      CaseState.PENDING_CASE_ISSUED,
    );

    const { createClaimSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const createClaimResponseSchema = await createClaimSchemaBuilder.buildFast1v2SS();
    ZodHelper.safeParse(createClaimResponseSchema, this.ccdCaseData);
    UserAssignedCasesHelper.addAssignedCaseToUser(claimantSolicitorUser, this.ccdCaseData?.id);
  }

  async CreateClaimFast2v1() {
    await this.setupUserData(claimantSolicitorUser);
    const { createClaimDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const createClaimData = await createClaimDataBuilder.buildFast2v1();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CREATE_CLAIM,
      createClaimData,
      CaseState.PENDING_CASE_ISSUED,
    );

    const { createClaimSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const createClaimResponseSchema = await createClaimSchemaBuilder.buildFast2v1();
    ZodHelper.safeParse(createClaimResponseSchema, this.ccdCaseData);
    UserAssignedCasesHelper.addAssignedCaseToUser(claimantSolicitorUser, this.ccdCaseData?.id);
  }

  async CreateClaimIntermediate1v1() {
    await this.setupUserData(claimantSolicitorUser);
    const { createClaimDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const createClaimData = await createClaimDataBuilder.buildIntermediate1v1();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CREATE_CLAIM,
      createClaimData,
      CaseState.PENDING_CASE_ISSUED,
    );

    const { createClaimSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const createClaimResponseSchema = await createClaimSchemaBuilder.buildIntermediate1v1();
    ZodHelper.safeParse(createClaimResponseSchema, this.ccdCaseData);
    UserAssignedCasesHelper.addAssignedCaseToUser(claimantSolicitorUser, this.ccdCaseData?.id);
  }

  async CreateClaimIntermediate1v2SS() {
    await this.setupUserData(claimantSolicitorUser);
    const { createClaimDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const createClaimData = await createClaimDataBuilder.buildIntermediate1v2SS();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CREATE_CLAIM,
      createClaimData,
      CaseState.PENDING_CASE_ISSUED,
    );

    const { createClaimSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const createClaimResponseSchema = await createClaimSchemaBuilder.buildIntermediate1v2SS();
    ZodHelper.safeParse(createClaimResponseSchema, this.ccdCaseData);
    UserAssignedCasesHelper.addAssignedCaseToUser(claimantSolicitorUser, this.ccdCaseData?.id);
  }

  async CreateClaimIntermediate2v1() {
    await this.setupUserData(claimantSolicitorUser);
    const { createClaimDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const createClaimData = await createClaimDataBuilder.buildIntermediate2v1();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CREATE_CLAIM,
      createClaimData,
      CaseState.PENDING_CASE_ISSUED,
    );

    const { createClaimSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const createClaimResponseSchema = await createClaimSchemaBuilder.buildIntermediate2v1();
    ZodHelper.safeParse(createClaimResponseSchema, this.ccdCaseData);
    UserAssignedCasesHelper.addAssignedCaseToUser(claimantSolicitorUser, this.ccdCaseData?.id);
  }

  async CreateClaimMulti1v1() {
    await this.setupUserData(claimantSolicitorUser);
    const { createClaimDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const createClaimData = await createClaimDataBuilder.buildMulti1v1();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CREATE_CLAIM,
      createClaimData,
      CaseState.PENDING_CASE_ISSUED,
    );

    const { createClaimSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const createClaimResponseSchema = await createClaimSchemaBuilder.buildMulti1v1();
    ZodHelper.safeParse(createClaimResponseSchema, this.ccdCaseData);
    UserAssignedCasesHelper.addAssignedCaseToUser(claimantSolicitorUser, this.ccdCaseData?.id);
  }

  async CreateClaimMulti2v1() {
    await this.setupUserData(claimantSolicitorUser);
    const { createClaimDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const createClaimData = await createClaimDataBuilder.buildMulti2v1();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CREATE_CLAIM,
      createClaimData,
      CaseState.PENDING_CASE_ISSUED,
    );

    const { createClaimSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const createClaimResponseSchema = await createClaimSchemaBuilder.buildMulti2v1();
    ZodHelper.safeParse(createClaimResponseSchema, this.ccdCaseData);
    UserAssignedCasesHelper.addAssignedCaseToUser(claimantSolicitorUser, this.ccdCaseData?.id);
  }

  async CreateClaimMulti1v2SS() {
    await this.setupUserData(claimantSolicitorUser);
    const { createClaimDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const createClaimData = await createClaimDataBuilder.buildMulti1v2SS();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CREATE_CLAIM,
      createClaimData,
      CaseState.PENDING_CASE_ISSUED,
    );

    const { createClaimSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const createClaimResponseSchema = await createClaimSchemaBuilder.buildMulti1v2SS();
    ZodHelper.safeParse(createClaimResponseSchema, this.ccdCaseData);
    UserAssignedCasesHelper.addAssignedCaseToUser(claimantSolicitorUser, this.ccdCaseData?.id);
  }

  async CreateClaimMulti1v2DS() {
    await this.setupUserData(claimantSolicitorUser);
    const { createClaimDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const createClaimData = await createClaimDataBuilder.buildMulti1v2DS();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CREATE_CLAIM,
      createClaimData,
      CaseState.PENDING_CASE_ISSUED,
    );

    const { createClaimSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const createClaimResponseSchema = await createClaimSchemaBuilder.buildMulti1v2DS();
    ZodHelper.safeParse(createClaimResponseSchema, this.ccdCaseData);
    UserAssignedCasesHelper.addAssignedCaseToUser(claimantSolicitorUser, this.ccdCaseData?.id);
  }

  async CreateClaimSmall2v1() {
    await this.setupUserData(claimantSolicitorUser);
    const { createClaimDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const createClaimData = await createClaimDataBuilder.buildSmall2v1();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CREATE_CLAIM,
      createClaimData,
      CaseState.PENDING_CASE_ISSUED,
    );

    const { createClaimSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const createClaimResponseSchema = await createClaimSchemaBuilder.buildSmall2v1();
    ZodHelper.safeParse(createClaimResponseSchema, this.ccdCaseData);
    UserAssignedCasesHelper.addAssignedCaseToUser(claimantSolicitorUser, this.ccdCaseData?.id);
  }

  async CreateClaimSmallTrack1v2SS() {
    await this.setupUserData(claimantSolicitorUser);
    const { createClaimDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const createClaimData = await createClaimDataBuilder.buildSmall1v2SS();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CREATE_CLAIM,
      createClaimData,
      CaseState.PENDING_CASE_ISSUED,
    );

    const { createClaimSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const createClaimResponseSchema = await createClaimSchemaBuilder.buildSmall1v2SS();
    ZodHelper.safeParse(createClaimResponseSchema, this.ccdCaseData);
    UserAssignedCasesHelper.addAssignedCaseToUser(claimantSolicitorUser, this.ccdCaseData?.id);
  }

  async CreateClaimSmallTrack1v2DS() {
    await this.setupUserData(claimantSolicitorUser);
    const { createClaimDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const createClaimData = await createClaimDataBuilder.buildSmall1v2DS();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CREATE_CLAIM,
      createClaimData,
      CaseState.PENDING_CASE_ISSUED,
    );

    const { createClaimSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const createClaimResponseSchema = await createClaimSchemaBuilder.buildSmall1v2DS();
    ZodHelper.safeParse(createClaimResponseSchema, this.ccdCaseData);
    UserAssignedCasesHelper.addAssignedCaseToUser(claimantSolicitorUser, this.ccdCaseData?.id);
  }

  async CreateClaimSmallTrack1vLIP() {
    await this.setupUserData(claimantSolicitorUser);
    const { createClaimDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const createClaimData = await createClaimDataBuilder.buildSmall1vLIP();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CREATE_CLAIM,
      createClaimData,
      CaseState.PENDING_CASE_ISSUED,
    );

    const { createClaimSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const createClaimResponseSchema = await createClaimSchemaBuilder.buildSmall1vLIP();
    ZodHelper.safeParse(createClaimResponseSchema, this.ccdCaseData);
    UserAssignedCasesHelper.addAssignedCaseToUser(claimantSolicitorUser, this.ccdCaseData?.id);
  }

  async CreateClaimSmallTrack1v2LIPs() {
    await this.setupUserData(claimantSolicitorUser);
    const { createClaimDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const createClaimData = await createClaimDataBuilder.buildSmall1v2LIPs();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CREATE_CLAIM,
      createClaimData,
      CaseState.PENDING_CASE_ISSUED,
    );

    const { createClaimSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const createClaimResponseSchema = await createClaimSchemaBuilder.buildSmall1v2LIPs();
    ZodHelper.safeParse(createClaimResponseSchema, this.ccdCaseData);
    UserAssignedCasesHelper.addAssignedCaseToUser(claimantSolicitorUser, this.ccdCaseData?.id);
  }

  async CreateClaimSmallTrack1v2LRLIP() {
    await this.setupUserData(claimantSolicitorUser);
    const { createClaimDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const createClaimData = await createClaimDataBuilder.buildSmall1v2LRLIP();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CREATE_CLAIM,
      createClaimData,
      CaseState.PENDING_CASE_ISSUED,
    );

    const { createClaimSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const createClaimResponseSchema = await createClaimSchemaBuilder.buildSmall1v2LRLIP();
    ZodHelper.safeParse(createClaimResponseSchema, this.ccdCaseData);
    UserAssignedCasesHelper.addAssignedCaseToUser(claimantSolicitorUser, this.ccdCaseData?.id);
  }

  async CreateClaimSmallTrack1v2LIPLR() {
    await this.setupUserData(claimantSolicitorUser);
    const { createClaimDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const createClaimData = await createClaimDataBuilder.buildSmall1v2LIPLR();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CREATE_CLAIM,
      createClaimData,
      CaseState.PENDING_CASE_ISSUED,
    );

    const { createClaimSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const createClaimResponseSchema = await createClaimSchemaBuilder.buildSmall1v2LIPLR();
    ZodHelper.safeParse(createClaimResponseSchema, this.ccdCaseData);
    UserAssignedCasesHelper.addAssignedCaseToUser(claimantSolicitorUser, this.ccdCaseData?.id);
  }

  async MakePaymentForClaimIssue() {
    await this.setupApiStep(claimantSolicitorUser);
    const { serviceRequestDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const paidServiceRequestDTO = await serviceRequestDataBuilder.buildPaidServiceRequestDTO(
      'paid',
      this.ccdCaseData?.id,
    );
    const { civilServiceRequests } = this.requestsFactory;
    await civilServiceRequests.updatePaymentForClaimIssue(
      claimantSolicitorUser,
      paidServiceRequestDTO,
    );
    await super.waitForFinishedBusinessProcess(this.ccdCaseData?.id);
    await super.fetchAndSetCCDCaseData();
  }

  async MakePaymentForHearingFee() {
    await this.setupApiStep(claimantSolicitorUser);
    const { civilServiceRequests } = this.requestsFactory;
    await civilServiceRequests.triggerHearingFeePaid(claimantSolicitorUser, this.ccdCaseData?.id);
    await super.waitForFinishedBusinessProcess(this.ccdCaseData?.id);
    await super.fetchAndSetCCDCaseData();
  }

  async AmendClaimDocuments() {
    await this.setupApiStep(claimantSolicitorUser);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { addOrAmendClaimDocumentsDataBuilder } =
      this.claimantDefendantSolicitorDataBuilderFactory;
    const addOrAmendClaimDocumentsData = await addOrAmendClaimDocumentsDataBuilder.buildData();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.ADD_OR_AMEND_CLAIM_DOCUMENTS,
      addOrAmendClaimDocumentsData,
      CaseState.CASE_ISSUED,
    );

    const { addOrAmendClaimDocumentsSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const addOrAmendClaimDocumentsSchema =
      await addOrAmendClaimDocumentsSchemaBuilder.buildSchema(caseDataBeforeSubmission);
    ZodHelper.safeParse(addOrAmendClaimDocumentsSchema, this.ccdCaseData);
  }

  async NotifyClaim() {
    await this.setupApiStep(claimantSolicitorUser);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { notifyClaimDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const notifyClaimData = await notifyClaimDataBuilder.build();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.NOTIFY_DEFENDANT_OF_CLAIM,
      notifyClaimData,
      CaseState.AWAITING_CASE_DETAILS_NOTIFICATION,
    );

    const { notifyClaimSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const notifyClaimSchema = await notifyClaimSchemaBuilder.buildSchema(caseDataBeforeSubmission);
    ZodHelper.safeParse(notifyClaimSchema, this.ccdCaseData);
  }

  async NotifyClaim1vLIP() {
    await this.setupApiStep(claimantSolicitorUser);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { notifyClaimDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const notifyClaimData = await notifyClaimDataBuilder.build1vLIP();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.NOTIFY_DEFENDANT_OF_CLAIM,
      notifyClaimData,
      CaseState.AWAITING_CASE_DETAILS_NOTIFICATION,
    );

    const { notifyClaimSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const notifyClaimSchema = await notifyClaimSchemaBuilder.build1vLIP(caseDataBeforeSubmission);
    ZodHelper.safeParse(notifyClaimSchema, this.ccdCaseData);
  }

  async NotifyClaim1v2LRLIP() {
    await this.setupApiStep(claimantSolicitorUser);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { notifyClaimDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const notifyClaimData = await notifyClaimDataBuilder.build1v2LRLIP();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.NOTIFY_DEFENDANT_OF_CLAIM,
      notifyClaimData,
      CaseState.AWAITING_CASE_DETAILS_NOTIFICATION,
    );

    const { notifyClaimSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const notifyClaimSchema = await notifyClaimSchemaBuilder.build1v2LRLIP(caseDataBeforeSubmission);
    ZodHelper.safeParse(notifyClaimSchema, this.ccdCaseData);
  }

  async NotifyClaim1v2LIPS() {
    await this.setupApiStep(claimantSolicitorUser);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { notifyClaimDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const notifyClaimData = await notifyClaimDataBuilder.build1v2LIPS();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.NOTIFY_DEFENDANT_OF_CLAIM,
      notifyClaimData,
      CaseState.AWAITING_CASE_DETAILS_NOTIFICATION,
    );

    const { notifyClaimSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const notifyClaimSchema = await notifyClaimSchemaBuilder.build1v2LIPS(caseDataBeforeSubmission);
    ZodHelper.safeParse(notifyClaimSchema, this.ccdCaseData);
  }

  async NotifyClaimDetails() {
    await this.setupApiStep(claimantSolicitorUser);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { notifyClaimDetailsDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const notifyClaimDetailsData = await notifyClaimDetailsDataBuilder.build();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS,
      notifyClaimDetailsData,
      CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT,
    );

    const { notifyClaimDetailsSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const notifyClaimDetailsSchema =
      await notifyClaimDetailsSchemaBuilder.buildSchema(caseDataBeforeSubmission);
    ZodHelper.safeParse(notifyClaimDetailsSchema, this.ccdCaseData);
  }

  async NotifyClaimDetails1vLIP() {
    await this.setupApiStep(claimantSolicitorUser);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { notifyClaimDetailsDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const notifyClaimDetailsData = await notifyClaimDetailsDataBuilder.build1vLIP();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS,
      notifyClaimDetailsData,
      CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT,
    );

    const { notifyClaimDetailsSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const notifyClaimDetailsSchema =
      await notifyClaimDetailsSchemaBuilder.build1vLIP(caseDataBeforeSubmission);
    ZodHelper.safeParse(notifyClaimDetailsSchema, this.ccdCaseData);
  }

  async NotifyClaimDetails1v2LRLIP() {
    await this.setupApiStep(claimantSolicitorUser);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { notifyClaimDetailsDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const notifyClaimDetailsData = await notifyClaimDetailsDataBuilder.build1v2LRLIP();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS,
      notifyClaimDetailsData,
      CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT,
    );

    const { notifyClaimDetailsSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const notifyClaimDetailsSchema =
      await notifyClaimDetailsSchemaBuilder.build1v2LRLIP(caseDataBeforeSubmission);
    ZodHelper.safeParse(notifyClaimDetailsSchema, this.ccdCaseData);
  }

  async NotifyClaimDetails1v2LIPS() {
    await this.setupApiStep(claimantSolicitorUser);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { notifyClaimDetailsDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const notifyClaimDetailsData = await notifyClaimDetailsDataBuilder.build1v2LIPS();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS,
      notifyClaimDetailsData,
      CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT,
    );

    const { notifyClaimDetailsSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const notifyClaimDetailsSchema =
      await notifyClaimDetailsSchemaBuilder.build1v2LIPS(caseDataBeforeSubmission);
    ZodHelper.safeParse(notifyClaimDetailsSchema, this.ccdCaseData);
  }

  async ManageContactInformation() {
    await this.setupApiStep(claimantSolicitorUser);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { manageContactInformationDataBuilder } =
      this.claimantDefendantSolicitorDataBuilderFactory;
    const manageContactInformationData =
      await manageContactInformationDataBuilder.buildClaimant();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.MANAGE_CONTACT_INFORMATION,
      manageContactInformationData,
    );

    const { manageContactInformationSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const manageContactInformationSchema =
      await manageContactInformationSchemaBuilder.build(caseDataBeforeSubmission);
    ZodHelper.safeParse(manageContactInformationSchema, this.ccdCaseData);
  }

  async AmendRespondent1ResponseDeadline() {
    await this.setupApiStep(claimantOrganisationSuperUser);
    const newRespondent1Deadline = DateHelper.subtractFromToday({ days: 1 });
    const dateString = DateHelper.formatDateToString(newRespondent1Deadline, {
      outputFormat: 'YYYY-MM-DDTHH:MM:SS',
    });
    const respondent1Deadline = { respondent1ResponseDeadline: dateString };
    const { civilServiceRequests } = this.requestsFactory;
    await civilServiceRequests.updateCaseData(
      claimantOrganisationSuperUser,
      respondent1Deadline,
      this.ccdCaseData?.id,
    );
    await super.fetchAndSetCCDCaseData();
  }

  async AmendRespondent2ResponseDeadline() {
    await this.setupApiStep(claimantOrganisationSuperUser);
    const newRespondent2Deadline = DateHelper.subtractFromToday({ days: 1 });
    const dateString = DateHelper.formatDateToString(newRespondent2Deadline, {
      outputFormat: 'YYYY-MM-DDTHH:MM:SS',
    });
    const respondent2ResponseDeadline = { respondent2ResponseDeadline: dateString };
    const { civilServiceRequests } = this.requestsFactory;
    await civilServiceRequests.updateCaseData(
      claimantOrganisationSuperUser,
      respondent2ResponseDeadline,
      this.ccdCaseData?.id,
    );
    await super.fetchAndSetCCDCaseData();
  }

  async RespondFastProceed() {
    await this.setupApiStep(claimantSolicitorUser);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { claimantResponseDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const claimantResponseEventData = await claimantResponseDataBuilder.buildFastFullDefence1v1();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CLAIMANT_RESPONSE,
      claimantResponseEventData,
      CaseState.JUDICIAL_REFERRAL,
    );

    const { claimantResponseSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const claimantResponseSchema =
      await claimantResponseSchemaBuilder.buildFastFullDefence1v1(caseDataBeforeSubmission);
    ZodHelper.safeParse(claimantResponseSchema, this.ccdCaseData);
  }

  async RespondSmallProceed() {
    await this.setupApiStep(claimantSolicitorUser);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { claimantResponseDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const claimantResponseEventData = await claimantResponseDataBuilder.buildSmallFullDefence1v1();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CLAIMANT_RESPONSE,
      claimantResponseEventData,
      CaseState.JUDICIAL_REFERRAL,
    );

    const { claimantResponseSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const claimantResponseSchema =
      await claimantResponseSchemaBuilder.buildSmallFullDefence1v1(caseDataBeforeSubmission);
    ZodHelper.safeParse(claimantResponseSchema, this.ccdCaseData);
  }

  async RespondFastProceed2v1() {
    await this.setupApiStep(claimantSolicitorUser);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { claimantResponseDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const claimantResponseEventData = await claimantResponseDataBuilder.buildFastFullDefence2v1();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CLAIMANT_RESPONSE,
      claimantResponseEventData,
      CaseState.JUDICIAL_REFERRAL,
    );

    const { claimantResponseSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const claimantResponseSchema =
      await claimantResponseSchemaBuilder.buildFastFullDefence2v1(caseDataBeforeSubmission);
    ZodHelper.safeParse(claimantResponseSchema, this.ccdCaseData);
  }

  async RespondIntermediateProceed2v1() {
    await this.setupApiStep(claimantSolicitorUser);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { claimantResponseDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const claimantResponseEventData =
      await claimantResponseDataBuilder.buildIntermediateFullDefence2v1();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CLAIMANT_RESPONSE,
      claimantResponseEventData,
      CaseState.JUDICIAL_REFERRAL,
    );

    const { claimantResponseSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const claimantResponseSchema =
      await claimantResponseSchemaBuilder.buildIntermediateFullDefence2v1(caseDataBeforeSubmission);
    ZodHelper.safeParse(claimantResponseSchema, this.ccdCaseData);
  }

  async RespondMultiProceed2v1() {
    await this.setupApiStep(claimantSolicitorUser);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { claimantResponseDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const claimantResponseEventData = await claimantResponseDataBuilder.buildMultiFullDefence2v1();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CLAIMANT_RESPONSE,
      claimantResponseEventData,
      CaseState.JUDICIAL_REFERRAL,
    );

    const { claimantResponseSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const claimantResponseSchema =
      await claimantResponseSchemaBuilder.buildMultiFullDefence2v1(caseDataBeforeSubmission);
    ZodHelper.safeParse(claimantResponseSchema, this.ccdCaseData);
  }

  async RespondFastProceed1v2SS() {
    await this.setupApiStep(claimantSolicitorUser);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { claimantResponseDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const claimantResponseEventData = await claimantResponseDataBuilder.buildFastProceed1v2SS();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CLAIMANT_RESPONSE,
      claimantResponseEventData,
      CaseState.JUDICIAL_REFERRAL,
    );

    const { claimantResponseSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const claimantResponseSchema =
      await claimantResponseSchemaBuilder.buildFastProceed1v2SS(caseDataBeforeSubmission);
    ZodHelper.safeParse(claimantResponseSchema, this.ccdCaseData);
  }

  async RespondIntermediateProceed1v2SS() {
    await this.setupApiStep(claimantSolicitorUser);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { claimantResponseDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const claimantResponseEventData =
      await claimantResponseDataBuilder.buildIntermediateProceed1v2SS();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CLAIMANT_RESPONSE,
      claimantResponseEventData,
      CaseState.JUDICIAL_REFERRAL,
    );

    const { claimantResponseSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const claimantResponseSchema =
      await claimantResponseSchemaBuilder.buildIntermediateProceed1v2SS(caseDataBeforeSubmission);
    ZodHelper.safeParse(claimantResponseSchema, this.ccdCaseData);
  }

  async RespondMultiProceed1v2SS() {
    await this.setupApiStep(claimantSolicitorUser);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { claimantResponseDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const claimantResponseEventData = await claimantResponseDataBuilder.buildMultiProceed1v2SS();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CLAIMANT_RESPONSE,
      claimantResponseEventData,
      CaseState.JUDICIAL_REFERRAL,
    );

    const { claimantResponseSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const claimantResponseSchema =
      await claimantResponseSchemaBuilder.buildMultiProceed1v2SS(caseDataBeforeSubmission);
    ZodHelper.safeParse(claimantResponseSchema, this.ccdCaseData);
  }

  async RespondFastProceed1v2DS() {
    await this.setupApiStep(claimantSolicitorUser);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { claimantResponseDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const claimantResponseEventData = await claimantResponseDataBuilder.buildFastFullDefence1v2DS();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CLAIMANT_RESPONSE,
      claimantResponseEventData,
      CaseState.JUDICIAL_REFERRAL,
    );

    const { claimantResponseSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const claimantResponseSchema =
      await claimantResponseSchemaBuilder.buildFastFullDefence1v2DS(caseDataBeforeSubmission);
    ZodHelper.safeParse(claimantResponseSchema, this.ccdCaseData);
  }

  async RespondMultiProceed1v2DS() {
    await this.setupApiStep(claimantSolicitorUser);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { claimantResponseDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const claimantResponseEventData =
      await claimantResponseDataBuilder.buildMultiFullDefence1v2DS();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CLAIMANT_RESPONSE,
      claimantResponseEventData,
      CaseState.JUDICIAL_REFERRAL,
    );

    const { claimantResponseSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const claimantResponseSchema =
      await claimantResponseSchemaBuilder.buildMultiFullDefence1v2DS(caseDataBeforeSubmission);
    ZodHelper.safeParse(claimantResponseSchema, this.ccdCaseData);
  }

  async RespondIntermediateProceed() {
    await this.setupApiStep(claimantSolicitorUser);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { claimantResponseDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const claimantResponseEventData =
      await claimantResponseDataBuilder.buildIntermediateFullDefence1v1();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CLAIMANT_RESPONSE,
      claimantResponseEventData,
      CaseState.JUDICIAL_REFERRAL,
    );

    const { claimantResponseSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const claimantResponseSchema =
      await claimantResponseSchemaBuilder.buildIntermediateFullDefence1v1(caseDataBeforeSubmission);
    ZodHelper.safeParse(claimantResponseSchema, this.ccdCaseData);
  }

  async RespondMultiProceed() {
    await this.setupApiStep(claimantSolicitorUser);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { claimantResponseDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const claimantResponseEventData = await claimantResponseDataBuilder.buildMultiFullDefence1v1();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.CLAIMANT_RESPONSE,
      claimantResponseEventData,
      CaseState.JUDICIAL_REFERRAL,
    );

    const { claimantResponseSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const claimantResponseSchema =
      await claimantResponseSchemaBuilder.buildMultiFullDefence1v1(caseDataBeforeSubmission);
    ZodHelper.safeParse(claimantResponseSchema, this.ccdCaseData);
  }

  async EvidenceUploadFast() {
    await this.setupApiStep(claimantSolicitorUser);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { evidenceUploadApplicantDataBuilder } =
      this.claimantDefendantSolicitorDataBuilderFactory;
    const evidenceUploadApplicantData = await evidenceUploadApplicantDataBuilder.buildFast();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.EVIDENCE_UPLOAD_APPLICANT,
      evidenceUploadApplicantData,
      CaseState.CASE_PROGRESSION,
    );

    const { evidenceUploadApplicantSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const evidenceUploadApplicantSchema =
      await evidenceUploadApplicantSchemaBuilder.buildFast(caseDataBeforeSubmission);
    ZodHelper.safeParse(evidenceUploadApplicantSchema, this.ccdCaseData);
  }

  async EvidenceUploadFast2v1() {
    await this.setupApiStep(claimantSolicitorUser);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { evidenceUploadApplicantDataBuilder } =
      this.claimantDefendantSolicitorDataBuilderFactory;
    const evidenceUploadApplicantData = await evidenceUploadApplicantDataBuilder.buildFast2v1();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.EVIDENCE_UPLOAD_APPLICANT,
      evidenceUploadApplicantData,
      CaseState.CASE_PROGRESSION,
    );

    const { evidenceUploadApplicantSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const evidenceUploadApplicantSchema =
      await evidenceUploadApplicantSchemaBuilder.buildFast2v1(caseDataBeforeSubmission);
    ZodHelper.safeParse(evidenceUploadApplicantSchema, this.ccdCaseData);
  }

  async EvidenceUploadSmall() {
    await this.setupApiStep(claimantSolicitorUser);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { evidenceUploadApplicantDataBuilder } =
      this.claimantDefendantSolicitorDataBuilderFactory;
    const evidenceUploadApplicantData = await evidenceUploadApplicantDataBuilder.buildSmall();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.EVIDENCE_UPLOAD_APPLICANT,
      evidenceUploadApplicantData,
      CaseState.CASE_PROGRESSION,
    );

    const { evidenceUploadApplicantSchemaBuilder } =
      this.claimantDefendantSolicitorSchemaBuilderFactory;
    const evidenceUploadApplicantSchema =
      await evidenceUploadApplicantSchemaBuilder.buildSmallClaim(caseDataBeforeSubmission);
    ZodHelper.safeParse(evidenceUploadApplicantSchema, this.ccdCaseData);
  }

  async DefaultJudgement() {
    await this.setupApiStep(claimantSolicitorUser);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { defaultJudgementDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defaultJudgementData = await defaultJudgementDataBuilder.build1v1();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.DEFAULT_JUDGEMENT,
      defaultJudgementData,
      CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT,
    );

    const { defaultJudgementSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defaultJudgementSchema =
      await defaultJudgementSchemaBuilder.build1v1(caseDataBeforeSubmission);
    ZodHelper.safeParse(defaultJudgementSchema, this.ccdCaseData);
  }

  async DefaultJudgement1v2SS() {
    await this.setupApiStep(claimantSolicitorUser);
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { defaultJudgementDataBuilder } = this.claimantDefendantSolicitorDataBuilderFactory;
    const defaultJudgementData = await defaultJudgementDataBuilder.build1v2SS();
    await super.submitCCDEvent(
      claimantSolicitorUser,
      ccdEvents.DEFAULT_JUDGEMENT,
      defaultJudgementData,
      CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT,
    );

    const { defaultJudgementSchemaBuilder } = this.claimantDefendantSolicitorSchemaBuilderFactory;
    const defaultJudgementSchema =
      await defaultJudgementSchemaBuilder.build1v2SS(caseDataBeforeSubmission);
    ZodHelper.safeParse(defaultJudgementSchema, this.ccdCaseData);
  }
}
