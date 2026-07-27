import BaseDataBuilder from '../../../../../base/base-data-builder';
import { claimantSolicitorUser } from '../../../../../config/users/exui-users';
import ClaimTrack from '../../../../../constants/cases/claim-track';
import ClaimType from '../../../../../constants/cases/claim-type';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import claimantResponseDataComponents from './claimant-response-data-components';

@AllMethodsStep({ methodNamesToIgnore: ['buildData'] })
export default class ClaimantResponseDataBuilder extends BaseDataBuilder {
  async buildSmallFullDefence1v1() {
    return this.buildData();
  }

  async buildFastFullDefence2v1() {
    return this.buildData({ claimTrack: ClaimTrack.FAST_CLAIM, claimType: ClaimType.TWO_VS_ONE });
  }

  async buildIntermediateFullDefence2v1() {
    return this.buildData({
      claimTrack: ClaimTrack.INTERMEDIATE_CLAIM,
      claimType: ClaimType.TWO_VS_ONE,
    });
  }

  async buildMultiFullDefence2v1() {
    return this.buildData({ claimTrack: ClaimTrack.MULTI_CLAIM, claimType: ClaimType.TWO_VS_ONE });
  }

  async buildFastProceed1v2SS() {
    return this.buildData({
      claimTrack: ClaimTrack.FAST_CLAIM,
      claimType: ClaimType.ONE_VS_TWO_SAME_SOL,
    });
  }

  async buildIntermediateProceed1v2SS() {
    return this.buildData({
      claimTrack: ClaimTrack.INTERMEDIATE_CLAIM,
      claimType: ClaimType.ONE_VS_TWO_SAME_SOL,
    });
  }

  async buildMultiProceed1v2SS() {
    return this.buildData({
      claimTrack: ClaimTrack.MULTI_CLAIM,
      claimType: ClaimType.ONE_VS_TWO_SAME_SOL,
    });
  }

  async buildFastFullDefence1v1() {
    return this.buildData({ claimTrack: ClaimTrack.FAST_CLAIM });
  }

  async buildIntermediateFullDefence1v1() {
    return this.buildData({ claimTrack: ClaimTrack.INTERMEDIATE_CLAIM });
  }

  async buildMultiFullDefence1v1() {
    return this.buildData({ claimTrack: ClaimTrack.MULTI_CLAIM });
  }

  async buildFastFullDefence1v2DS() {
    return this.buildData({
      claimTrack: ClaimTrack.FAST_CLAIM,
      claimType: ClaimType.ONE_VS_TWO_DIFF_SOL,
    });
  }

  async buildMultiFullDefence1v2DS() {
    return this.buildData({
      claimTrack: ClaimTrack.MULTI_CLAIM,
      claimType: ClaimType.ONE_VS_TWO_DIFF_SOL,
    });
  }

  protected async buildData({
    claimTrack = ClaimTrack.SMALL_CLAIM,
    claimType = ClaimType.ONE_VS_ONE,
  }: {
    claimTrack?: ClaimTrack;
    claimType?: ClaimType;
  } = {}) {
    const { civilServiceRequests } = this.requestsFactory;
    let frcSupportingDocument;
    const defenceResponseDocument1 =
      await civilServiceRequests.uploadTestDocument(claimantSolicitorUser);
    let defenceResponseDocument2;
    if (claimType === ClaimType.ONE_VS_TWO_DIFF_SOL) {
      defenceResponseDocument2 =
        await civilServiceRequests.uploadTestDocument(claimantSolicitorUser);
    }
    const draftDirectionsDocument =
      await civilServiceRequests.uploadTestDocument(claimantSolicitorUser);
    if (claimTrack === ClaimTrack.INTERMEDIATE_CLAIM) {
      frcSupportingDocument = await civilServiceRequests.uploadTestDocument(claimantSolicitorUser);
    }

    return {
      ...claimantResponseDataComponents.respondentResponse(claimType),
      ...claimantResponseDataComponents.applicantDefenceResponseDocument(
        claimType,
        defenceResponseDocument1,
        defenceResponseDocument2!,
      ),
      ...claimantResponseDataComponents.fileDirectionsQuestionnaire(claimTrack),
      ...claimantResponseDataComponents.fixedRecoverableCosts(claimTrack),
      ...claimantResponseDataComponents.fixedRecoverableCostsIntermediate(
        claimTrack,
        frcSupportingDocument,
      ),
      ...claimantResponseDataComponents.disclosureOfElectronicDocuments(claimTrack),
      ...claimantResponseDataComponents.disclosureOfNonElectronicDocuments(claimTrack),
      ...claimantResponseDataComponents.disclosureReport(claimTrack),
      ...claimantResponseDataComponents.deterWithHearing(claimTrack),
      ...claimantResponseDataComponents.experts,
      ...claimantResponseDataComponents.witnesses,
      ...claimantResponseDataComponents.language,
      ...claimantResponseDataComponents.hearing,
      ...claimantResponseDataComponents.draftDirections(draftDirectionsDocument),
      ...claimantResponseDataComponents.hearingSupport,
      ...claimantResponseDataComponents.vulnerabilityQuestions,
      ...claimantResponseDataComponents.furtherInformation,
      ...claimantResponseDataComponents.statementOfTruth,
    };
  }
}
