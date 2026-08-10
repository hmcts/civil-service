import BaseDataBuilder from '../../../../../base/base-data-builder';
import {
  defendantSolicitor1User,
  defendantSolicitor2User,
} from '../../../../../config/users/exui-users';
import ClaimTrack from '../../../../../constants/cases/claim-track';
import ClaimType from '../../../../../constants/cases/claim-type';
import DefendantResponseType from '../../../../../constants/ccd-events/defendant-response/unspec/defendant-response-type';
import partys from '../../../../../constants/users/partys';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import { Party } from '../../../../../models/users/partys';
import defendantResponseDataComponents from './defendant-response-data-components';

@AllMethodsStep({ methodNamesToIgnore: ['buildData'] })
export default class DefendantResponseDataBuilder extends BaseDataBuilder {
  async buildDS1SmallFullDefence1v1() {
    return this.buildData();
  }

  async buildDS1FastFullDefence2v1() {
    return this.buildData({
      claimTrack: ClaimTrack.FAST_CLAIM,
      claimType: ClaimType.TWO_VS_ONE,
    });
  }

  async buildDS1IntermediateFullDefence2v1() {
    return this.buildData({
      claimTrack: ClaimTrack.INTERMEDIATE_CLAIM,
      claimType: ClaimType.TWO_VS_ONE,
    });
  }

  async buildDS1MultiFullDefence2v1() {
    return this.buildData({
      claimTrack: ClaimTrack.MULTI_CLAIM,
      claimType: ClaimType.TWO_VS_ONE,
    });
  }

  async buildDS1FastFullDefence1v2SS() {
    return this.buildData({
      claimTrack: ClaimTrack.FAST_CLAIM,
      claimType: ClaimType.ONE_VS_TWO_SAME_SOL,
    });
  }

  async buildDS1IntermediateFullDefence1v2SS() {
    return this.buildData({
      claimTrack: ClaimTrack.INTERMEDIATE_CLAIM,
      claimType: ClaimType.ONE_VS_TWO_SAME_SOL,
    });
  }

  async buildDS1MultiFullDefence1v2SS() {
    return this.buildData({
      claimTrack: ClaimTrack.MULTI_CLAIM,
      claimType: ClaimType.ONE_VS_TWO_SAME_SOL,
    });
  }

  async buildDS1FastTrackFullDefence1v2DS() {
    return this.buildData({
      claimTrack: ClaimTrack.FAST_CLAIM,
      claimType: ClaimType.ONE_VS_TWO_DIFF_SOL,
    });
  }

  async buildDS1MultiFullDefence1v2DS() {
    return this.buildData({
      claimTrack: ClaimTrack.MULTI_CLAIM,
      claimType: ClaimType.ONE_VS_TWO_DIFF_SOL,
    });
  }

  async buildDS1FastFullDefence() {
    return this.buildData({ claimTrack: ClaimTrack.FAST_CLAIM });
  }

  async buildDS1IntermediateFullDefence() {
    return this.buildData({ claimTrack: ClaimTrack.INTERMEDIATE_CLAIM });
  }

  async buildDS1MultiFullDefence() {
    return this.buildData({ claimTrack: ClaimTrack.MULTI_CLAIM });
  }

  async buildDS2FastFullDefence() {
    return this.buildData({
      claimTrack: ClaimTrack.FAST_CLAIM,
      defendantSolicitorParty: partys.DEFENDANT_SOLICITOR_2,
    });
  }

  async buildDS2FastTrackFullDefence1v2DS() {
    return this.buildData({
      claimTrack: ClaimTrack.FAST_CLAIM,
      claimType: ClaimType.ONE_VS_TWO_DIFF_SOL,
      defendantSolicitorParty: partys.DEFENDANT_SOLICITOR_2,
    });
  }

  async buildDS2MultiFullDefence1v2DS() {
    return this.buildData({
      claimTrack: ClaimTrack.MULTI_CLAIM,
      claimType: ClaimType.ONE_VS_TWO_DIFF_SOL,
      defendantSolicitorParty: partys.DEFENDANT_SOLICITOR_2,
    });
  }

  protected async buildData({
    claimType = ClaimType.ONE_VS_ONE,
    claimTrack = ClaimTrack.SMALL_CLAIM,
    responseType = DefendantResponseType.FULL_DEFENCE,
    defendantSolicitorParty = partys.DEFENDANT_SOLICITOR_1,
  }: {
    claimType?: ClaimType;
    claimTrack?: ClaimTrack;
    responseType?: DefendantResponseType;
    defendantSolicitorParty?: Party;
  } = {}) {
    const { civilServiceRequests } = this.requestsFactory;
    let frcSupportingDocument;
    const defendantSolicitorUser =
      defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? defendantSolicitor1User
        : defendantSolicitor2User;
    const defenceDocument = await civilServiceRequests.uploadTestDocument(defendantSolicitorUser);
    const draftDirectionsDocument =
      await civilServiceRequests.uploadTestDocument(defendantSolicitorUser);
    if (claimTrack === ClaimTrack.INTERMEDIATE_CLAIM) {
      frcSupportingDocument = await civilServiceRequests.uploadTestDocument(defendantSolicitorUser);
    }

    const eventData: Record<string, unknown> = {};

    Object.assign(
      eventData,
      defendantResponseDataComponents.confirmDetails(
        claimType,
        this.ccdCaseData,
        defendantSolicitorParty,
      ),
      defendantResponseDataComponents.singleResponse(claimType),
      defendantResponseDataComponents.respondentResponseType(
        claimType,
        responseType,
        defendantSolicitorParty,
      ),
      defendantResponseDataComponents.solicitorReferences(
        this.ccdCaseData,
        defendantSolicitorParty,
      ),
      defendantResponseDataComponents.upload(defenceDocument, defendantSolicitorParty),
      defendantResponseDataComponents.fileDirectionsQuestionnaire(
        claimTrack,
        defendantSolicitorParty,
      ),
      defendantResponseDataComponents.fixedRecoverableCosts(claimTrack, defendantSolicitorParty),
      defendantResponseDataComponents.fixedRecoverableCostsIntermediate(
        claimTrack,
        defendantSolicitorParty,
        frcSupportingDocument,
      ),
      defendantResponseDataComponents.disclosureOfElectronicDocuments(
        claimTrack,
        defendantSolicitorParty,
      ),
      defendantResponseDataComponents.disclosureOfNonElectronicDocuments(
        claimTrack,
        defendantSolicitorParty,
      ),
      defendantResponseDataComponents.deterWithoutHearing(claimTrack, defendantSolicitorParty),
      defendantResponseDataComponents.experts(defendantSolicitorParty),
      defendantResponseDataComponents.witnesses(defendantSolicitorParty),
      defendantResponseDataComponents.language(defendantSolicitorParty),
      defendantResponseDataComponents.hearing(defendantSolicitorParty),
      defendantResponseDataComponents.draftDirections(
        claimTrack,
        draftDirectionsDocument,
        defendantSolicitorParty,
      ),
      defendantResponseDataComponents.requestedCourt(defendantSolicitorParty),
      defendantResponseDataComponents.hearingSupport(defendantSolicitorParty),
      defendantResponseDataComponents.vulnerabilityQuestions(defendantSolicitorParty),
      defendantResponseDataComponents.furtherInformation(defendantSolicitorParty),
      defendantResponseDataComponents.statementOfTruth(defendantSolicitorParty),
      defendantResponseDataComponents.undefine(defendantSolicitorParty),
    );

    return eventData;
  }
}
