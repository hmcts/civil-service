import BaseDataBuilder from '../../../../../base/base-data-builder';
import { claimantSolicitorUser } from '../../../../../config/users/exui-users';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import claimantResponseSpecData from './claimant-response-spec-data-components';
import ClaimType from '../../../../../constants/cases/claim-type';
import ClaimantResponseSpecType from '../../../../../constants/ccd-events/claimant-response-spec-type/claimant-response-spec-type';
import ClaimTrack from '../../../../../constants/cases/claim-track';
@AllMethodsStep({ methodNamesToIgnore: ['buildData'] })
export default class ClaimantResponseSpecDataBuilder extends BaseDataBuilder {
  async buildFastRejectFullDefence() {
    return this.buildData({ claimTrack: ClaimTrack.FAST_CLAIM });
  }

  async buildIntermediateRejectFullDefence() {
    return this.buildData({ claimTrack: ClaimTrack.INTERMEDIATE_CLAIM });
  }

  async buildMultiRejectFullDefence() {
    return this.buildData({ claimTrack: ClaimTrack.MULTI_CLAIM });
  }

  async buildMultiRejectFullDefence1v2SS() {
    return this.buildData({
      claimType: ClaimType.ONE_VS_TWO_SAME_SOL,
      claimTrack: ClaimTrack.MULTI_CLAIM,
    });
  }

  async buildMultiRejectFullDefence1v2DS() {
    return this.buildData({
      claimType: ClaimType.ONE_VS_TWO_DIFF_SOL,
      claimTrack: ClaimTrack.MULTI_CLAIM,
    });
  }

  async buildFastRejectPartAdmit() {
    return this.buildData({
      claimTrack: ClaimTrack.FAST_CLAIM,
      claimantResponseType: ClaimantResponseSpecType.REJECT_PART_ADMIT,
    });
  }

  async buildSmallRejectPartAdmitPaidConfirmNotPaid() {
    return this.buildData({
      claimTrack: ClaimTrack.SMALL_CLAIM,
      claimantResponseType: ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_NOT_PAID,
    });
  }

  async buildSmallRejectPartAdmitPaidConfirmPaid() {
    return this.buildData({
      claimTrack: ClaimTrack.SMALL_CLAIM,
      claimantResponseType: ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_PAID,
    });
  }

  async buildIntermediateRejectPartAdmit() {
    return this.buildData({
      claimTrack: ClaimTrack.INTERMEDIATE_CLAIM,
      claimantResponseType: ClaimantResponseSpecType.REJECT_PART_ADMIT,
    });
  }

  async buildMultiRejectPartAdmit() {
    return this.buildData({
      claimTrack: ClaimTrack.MULTI_CLAIM,
      claimantResponseType: ClaimantResponseSpecType.REJECT_PART_ADMIT,
    });
  }

  async buildFullAdmitImmediately() {
    return this.buildData({
      claimantResponseType: ClaimantResponseSpecType.ACCEPT_FULL_ADMIT,
    });
  }

  async buildFullAdmitSetDate() {
    return this.buildData({
      claimantResponseType: ClaimantResponseSpecType.ACCEPT_FULL_ADMIT,
    });
  }

  async buildFullAdmitRepayment() {
    return this.buildData({
      claimantResponseType: ClaimantResponseSpecType.ACCEPT_FULL_ADMIT,
    });
  }

  async buildSmallRejectPartAdmit() {
    return this.buildData({
      claimTrack: ClaimTrack.SMALL_CLAIM,
      claimantResponseType: ClaimantResponseSpecType.REJECT_PART_ADMIT,
    });
  }

  async buildFastRejectFullDefence2v1() {
    return this.buildData({ claimType: ClaimType.TWO_VS_ONE, claimTrack: ClaimTrack.FAST_CLAIM });
  }

  async buildFastAcceptFullDefence2v1() {
    return this.buildData({
      claimType: ClaimType.TWO_VS_ONE,
      claimTrack: ClaimTrack.FAST_CLAIM,
      claimantResponseType: ClaimantResponseSpecType.ACCEPT_FULL_DEFENCE,
    });
  }

  async buildFastRejectFullDefence1v2SS() {
    return this.buildData({
      claimType: ClaimType.ONE_VS_TWO_SAME_SOL,
      claimTrack: ClaimTrack.FAST_CLAIM,
    });
  }

  async buildFastRejectFullDefence1v2DS() {
    return this.buildData({
      claimType: ClaimType.ONE_VS_TWO_DIFF_SOL,
      claimTrack: ClaimTrack.FAST_CLAIM,
    });
  }

  async buildFastAcceptFullDefence1v2SS() {
    return this.buildData({
      claimType: ClaimType.ONE_VS_TWO_SAME_SOL,
      claimTrack: ClaimTrack.FAST_CLAIM,
      claimantResponseType: ClaimantResponseSpecType.ACCEPT_FULL_DEFENCE,
    });
  }

  async buildSmallRejectFullDefence() {
    return this.buildData({ claimTrack: ClaimTrack.SMALL_CLAIM });
  }

  async buildSmallRejectFullDefence2v1() {
    return this.buildData({
      claimType: ClaimType.TWO_VS_ONE,
      claimTrack: ClaimTrack.SMALL_CLAIM,
    });
  }

  async buildSmallRejectFullDefence1v2SS() {
    return this.buildData({
      claimType: ClaimType.ONE_VS_TWO_SAME_SOL,
      claimTrack: ClaimTrack.SMALL_CLAIM,
    });
  }

  async buildSmallRejectFullDefence1v2DS() {
    return this.buildData({
      claimType: ClaimType.ONE_VS_TWO_DIFF_SOL,
      claimTrack: ClaimTrack.SMALL_CLAIM,
    });
  }

  protected async buildData({
    claimType = ClaimType.ONE_VS_ONE,
    claimTrack = ClaimTrack.FAST_CLAIM,
    claimantResponseType = ClaimantResponseSpecType.REJECT_FULL_DEFENCE,
  }: {
    claimType?: ClaimType;
    claimTrack?: ClaimTrack;
    claimantResponseType?: ClaimantResponseSpecType;
  } = {}) {
    const { civilServiceRequests } = this.requestsFactory;
    const defenceResponseDocumentSpec =
      (claimantResponseType === ClaimantResponseSpecType.REJECT_FULL_DEFENCE ||
      claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT ||
      claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_NOT_PAID ||
      claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_PAID)
        ? await civilServiceRequests.uploadTestDocument(claimantSolicitorUser)
        : undefined;
    const frcSupportingDocument =
      claimTrack === ClaimTrack.INTERMEDIATE_CLAIM
        ? await civilServiceRequests.uploadTestDocument(claimantSolicitorUser)
        : undefined;

    const eventData: Record<string, unknown> = {};

    Object.assign(
      eventData,
      claimantResponseSpecData.undefine(claimantResponseType),
      claimantResponseSpecData.defendantResponse(
        claimType,
        claimantResponseType,
      ),
      claimantResponseSpecData.intentionToSettle(claimantResponseType),
      claimantResponseSpecData.claimantDefenceResponseDocument(
        defenceResponseDocumentSpec,
        claimantResponseType,
      ),
      claimantResponseSpecData.mediationContactInformation(
        claimTrack,
        claimantResponseType,
      ),
      claimantResponseSpecData.mediationAvailability(
        claimTrack,
        claimantResponseType,
      ),
      claimantResponseSpecData.determinationWithoutHearing(
        claimTrack,
        claimantResponseType,
      ),
      claimantResponseSpecData.fileDirectionsQuestionnaire(
        claimTrack,
        claimantResponseType,
      ),
      claimantResponseSpecData.fixedRecoverableCosts(
        claimTrack,
        claimantResponseType,
      ),
      claimantResponseSpecData.fixedRecoverableCostsIntermediate(
        claimTrack,
        claimantResponseType,
        frcSupportingDocument,
      ),
      claimantResponseSpecData.disclosureOfElectronicDocuments(
        claimTrack,
        claimantResponseType,
      ),
      claimantResponseSpecData.disclosureOfNonElectronicDocuments(
        claimTrack,
        claimantResponseType,
      ),
      claimantResponseSpecData.disclosureReport(
        claimTrack,
        claimantResponseType,
      ),
      claimantResponseSpecData.experts(claimTrack, claimantResponseType),
      claimantResponseSpecData.witnesses(
        claimTrack,
        claimantResponseType,
      ),
      claimantResponseSpecData.language(claimantResponseType),
      claimantResponseSpecData.hearing(claimTrack, claimantResponseType),
      claimantResponseSpecData.requestedCourtLocation(
        claimantResponseType,
      ),
      claimantResponseSpecData.hearingSupport(claimantResponseType),
      claimantResponseSpecData.vulnerabilityQuestions(claimantResponseType),
      claimantResponseSpecData.application(
        claimTrack,
        claimantResponseType,
      ),
      claimantResponseSpecData.statementOfTruth,
    );

    return eventData;
  }
}
