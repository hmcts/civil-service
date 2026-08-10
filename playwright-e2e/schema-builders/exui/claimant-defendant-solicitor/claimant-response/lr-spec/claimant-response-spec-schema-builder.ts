import { z } from 'zod';
import BaseSchemaBuilder from '../../../../../base/base-schema-builder';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import ClaimType from '../../../../../constants/cases/claim-type';
import ClaimTrack from '../../../../../constants/cases/claim-track';
import ClaimantResponseSpecType from '../../../../../constants/ccd-events/claimant-response-spec-type/claimant-response-spec-type';
import ZodHelper from '../../../../../helpers/zod-helper';
import CCDCaseData from '../../../../../models/ccd-case-data';
import claimantResponseSpecSchemaComponents from './claimant-response-spec-schema-components';

@AllMethodsStep({ methodNamesToIgnore: ['buildSchema'] })
export default class ClaimantResponseSpecSchemaBuilder extends BaseSchemaBuilder {
  async buildFastRejectFullDefence(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, { claimTrack: ClaimTrack.FAST_CLAIM });
  }

  async buildIntermediateRejectFullDefence(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {
      claimTrack: ClaimTrack.INTERMEDIATE_CLAIM,
    });
  }

  async buildMultiRejectFullDefence(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {
      claimTrack: ClaimTrack.MULTI_CLAIM,
    });
  }

  async buildMultiRejectFullDefence1v2SS(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {
      claimType: ClaimType.ONE_VS_TWO_SAME_SOL,
      claimTrack: ClaimTrack.MULTI_CLAIM,
    });
  }

  async buildMultiRejectFullDefence1v2DS(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {
      claimType: ClaimType.ONE_VS_TWO_DIFF_SOL,
      claimTrack: ClaimTrack.MULTI_CLAIM,
    });
  }

  async buildFastRejectPartAdmit(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {
      claimTrack: ClaimTrack.FAST_CLAIM,
      claimantResponseType: ClaimantResponseSpecType.REJECT_PART_ADMIT,
    });
  }

  async buildSmallRejectPartAdmitHasPaidConfirmNotPaid(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {
      claimTrack: ClaimTrack.SMALL_CLAIM,
      claimantResponseType: ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_NOT_PAID,
    });
  }

  async buildSmallRejectPartAdmitPaidConfirmPaid(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {
      claimTrack: ClaimTrack.SMALL_CLAIM,
      claimantResponseType: ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_PAID,
    });
  }

  async buildIntermediateRejectPartAdmit(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {
      claimTrack: ClaimTrack.INTERMEDIATE_CLAIM,
      claimantResponseType: ClaimantResponseSpecType.REJECT_PART_ADMIT,
    });
  }

  async buildMultiRejectPartAdmit(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {
      claimTrack: ClaimTrack.MULTI_CLAIM,
      claimantResponseType: ClaimantResponseSpecType.REJECT_PART_ADMIT,
    });
  }

  async buildFullAdmitImmediately(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {
      claimantResponseType: ClaimantResponseSpecType.ACCEPT_FULL_ADMIT,
    });
  }

  async buildFullAdmitSetDate(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {
      claimantResponseType: ClaimantResponseSpecType.ACCEPT_FULL_ADMIT,
    });
  }

  async buildFullAdmitRepayment(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {
      claimantResponseType: ClaimantResponseSpecType.ACCEPT_FULL_ADMIT,
    });
  }

  async buildSmallRejectPartAdmit(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {
      claimTrack: ClaimTrack.SMALL_CLAIM,
      claimantResponseType: ClaimantResponseSpecType.REJECT_PART_ADMIT,
    });
  }

  async buildFastRejectFullDefence2v1(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {
      claimType: ClaimType.TWO_VS_ONE,
      claimTrack: ClaimTrack.FAST_CLAIM,
    });
  }

  async buildFastAcceptFullDefence2v1(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {
      claimType: ClaimType.TWO_VS_ONE,
      claimTrack: ClaimTrack.FAST_CLAIM,
      claimantResponseType: ClaimantResponseSpecType.ACCEPT_FULL_DEFENCE,
    });
  }

  async buildFastRejectFullDefence1v2SS(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {
      claimType: ClaimType.ONE_VS_TWO_SAME_SOL,
      claimTrack: ClaimTrack.FAST_CLAIM,
    });
  }

  async buildFastRejectFullDefence1v2DS(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {
      claimType: ClaimType.ONE_VS_TWO_DIFF_SOL,
      claimTrack: ClaimTrack.FAST_CLAIM,
    });
  }

  async buildFastAcceptFullDefence1v2SS(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {
      claimType: ClaimType.ONE_VS_TWO_SAME_SOL,
      claimTrack: ClaimTrack.FAST_CLAIM,
      claimantResponseType: ClaimantResponseSpecType.ACCEPT_FULL_DEFENCE,
    });
  }

  async buildSmallRejectFullDefence(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, { claimTrack: ClaimTrack.SMALL_CLAIM });
  }

  async buildSmallRejectFullDefence2v1(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {
      claimType: ClaimType.TWO_VS_ONE,
      claimTrack: ClaimTrack.SMALL_CLAIM,
    });
  }

  async buildSmallRejectFullDefence1v2SS(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {
      claimType: ClaimType.ONE_VS_TWO_SAME_SOL,
      claimTrack: ClaimTrack.SMALL_CLAIM,
    });
  }

  async buildSmallRejectFullDefence1v2DS(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {
      claimType: ClaimType.ONE_VS_TWO_DIFF_SOL,
      claimTrack: ClaimTrack.SMALL_CLAIM,
    });
  }

  protected async buildSchema(
    caseDataBeforeSubmission?: CCDCaseData,
    {
      claimType = ClaimType.ONE_VS_ONE,
      claimTrack = ClaimTrack.FAST_CLAIM,
      claimantResponseType = ClaimantResponseSpecType.REJECT_FULL_DEFENCE,
    }: {
      claimType?: ClaimType;
      claimTrack?: ClaimTrack;
      claimantResponseType?: ClaimantResponseSpecType;
    } = {},
  ): Promise<z.ZodType> {
    const baseSchema = ZodHelper.createSchemaFromJson(caseDataBeforeSubmission, {
      strictObjects: false,
    }) as z.ZodObject<any>;
    const schemaShape: Record<string, z.ZodType> = {};

    Object.assign(
      schemaShape,
      claimantResponseSpecSchemaComponents.undefine,
      claimantResponseSpecSchemaComponents.defendantResponse(
        claimType,
        claimantResponseType,
      ),
      claimantResponseSpecSchemaComponents.intentionToSettle(claimantResponseType),
      claimantResponseSpecSchemaComponents.claimantDefenceResponseDocument(
        claimantResponseType,
      ),
      claimantResponseSpecSchemaComponents.mediationContactInformation(
        claimTrack,
        claimantResponseType,
      ),
      claimantResponseSpecSchemaComponents.mediationAvailability(
        claimTrack,
        claimantResponseType,
      ),
      claimantResponseSpecSchemaComponents.determinationWithoutHearing(
        claimTrack,
        claimantResponseType,
      ),
      claimantResponseSpecSchemaComponents.fileDirectionsQuestionnaire(
        claimTrack,
        claimantResponseType,
      ),
      claimantResponseSpecSchemaComponents.fixedRecoverableCosts(
        claimTrack,
        claimantResponseType,
      ),
      claimantResponseSpecSchemaComponents.fixedRecoverableCostsIntermediate(
        claimTrack,
        claimantResponseType,
      ),
      claimantResponseSpecSchemaComponents.disclosureOfElectronicDocuments(
        claimTrack,
        claimantResponseType,
      ),
      claimantResponseSpecSchemaComponents.disclosureOfNonElectronicDocuments(
        claimTrack,
        claimantResponseType,
      ),
      claimantResponseSpecSchemaComponents.disclosureReport(
        claimTrack,
        claimantResponseType,
      ),
      claimantResponseSpecSchemaComponents.experts(
        claimTrack,
        claimantResponseType,
      ),
      claimantResponseSpecSchemaComponents.witnesses(
        claimTrack,
        claimantResponseType,
      ),
      claimantResponseSpecSchemaComponents.language(
        claimantResponseType,
      ),
      claimantResponseSpecSchemaComponents.hearing(
        claimTrack,
        claimantResponseType,
      ),
      claimantResponseSpecSchemaComponents.requestedCourtLocation(
        claimantResponseType,
      ),
      claimantResponseSpecSchemaComponents.hearingSupport(
        claimantResponseType,
      ),
      claimantResponseSpecSchemaComponents.vulnerabilityQuestions(claimantResponseType),
      claimantResponseSpecSchemaComponents.application(
        claimTrack,
        claimantResponseType,
      ),
    );

    return baseSchema.extend(schemaShape);
  }
}
