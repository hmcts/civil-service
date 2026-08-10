import BaseDataBuilder from '../../../../../base/base-data-builder';
import { claimantSolicitorUser } from '../../../../../config/users/exui-users';
import claimantDefendantPartyTypes from '../../../../../constants/users/claimant-defendant-party-types';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import ClaimTypeUnspec from '../../../../../constants/ccd-events/create-claim/claim-type-unspec';
import PersonalInjuryType from '../../../../../constants/ccd-events/create-claim/personal-injury-type';
import ClaimTrack from '../../../../../constants/cases/claim-track';
import ClaimType from '../../../../../constants/cases/claim-type';
import ClaimTypeHelper from '../../../../../helpers/claim-type-helper';
import { UploadDocumentValue } from '../../../../../models/ccd-case-data';
import { ClaimantDefendantPartyType } from '../../../../../models/users/claimant-defendant-party-types';
import PersonalInjuryClaimTypeUnspecObjs from '../../../../../models/ccd-events/create-claim/claim-type-unspec-objs';
import createClaimData from './create-claim-data-components';
@AllMethodsStep({ methodNamesToIgnore: ['buildData'] })
export default class CreateClaimDataBuilder extends BaseDataBuilder {
  async buildFast1v1() {
    return this.buildData({ claimTrack: ClaimTrack.FAST_CLAIM });
  }

  async buildFastNIHL1v1() {
    return this.buildData({
      claimTrack: ClaimTrack.FAST_CLAIM,
      claimTypeUnspec: {
        claimTypeUnspec: ClaimTypeUnspec.PERSONAL_INJURY,
        personalInjuryType: PersonalInjuryType.NOISE_INDUCED_HEARING_LOSS,
      },
    });
  }

  async buildFast1v2DS() {
    return this.buildData({
      claimType: ClaimType.ONE_VS_TWO_DIFF_SOL,
      claimTrack: ClaimTrack.FAST_CLAIM,
    });
  }

  async buildFast1v2SS() {
    return this.buildData({
      claimType: ClaimType.ONE_VS_TWO_SAME_SOL,
      claimTrack: ClaimTrack.FAST_CLAIM,
    });
  }

  async buildFast2v1() {
    return this.buildData({
      claimType: ClaimType.TWO_VS_ONE,
      claimTrack: ClaimTrack.FAST_CLAIM,
    });
  }

  async buildIntermediate1v1() {
    return this.buildData({ claimTrack: ClaimTrack.INTERMEDIATE_CLAIM });
  }

  async buildIntermediate1v2SS() {
    return this.buildData({
      claimType: ClaimType.ONE_VS_TWO_SAME_SOL,
      claimTrack: ClaimTrack.INTERMEDIATE_CLAIM,
    });
  }

  async buildIntermediate2v1() {
    return this.buildData({
      claimType: ClaimType.TWO_VS_ONE,
      claimTrack: ClaimTrack.INTERMEDIATE_CLAIM,
    });
  }

  async buildMulti1v1() {
    return this.buildData({ claimTrack: ClaimTrack.MULTI_CLAIM });
  }

  async buildMulti2v1() {
    return this.buildData({
      claimType: ClaimType.TWO_VS_ONE,
      claimTrack: ClaimTrack.MULTI_CLAIM,
    });
  }

  async buildMulti1v2SS() {
    return this.buildData({
      claimType: ClaimType.ONE_VS_TWO_SAME_SOL,
      claimTrack: ClaimTrack.MULTI_CLAIM,
    });
  }

  async buildMulti1v2DS() {
    return this.buildData({
      claimType: ClaimType.ONE_VS_TWO_DIFF_SOL,
      claimTrack: ClaimTrack.MULTI_CLAIM,
    });
  }

  async buildSmall1v1() {
    return this.buildData();
  }

  async buildSmall2v1() {
    return this.buildData({ claimType: ClaimType.TWO_VS_ONE });
  }

  async buildSmall1v2SS() {
    return this.buildData({ claimType: ClaimType.ONE_VS_TWO_SAME_SOL });
  }

  async buildSmall1v2DS() {
    return this.buildData({ claimType: ClaimType.ONE_VS_TWO_DIFF_SOL });
  }

  async buildSmall1vLIP() {
    return this.buildData({ claimType: ClaimType.ONE_VS_ONE_LIP });
  }

  async buildSmall1v2LIPs() {
    return this.buildData({ claimType: ClaimType.ONE_VS_TWO_LIPS });
  }

  async buildSmall1v2LIPLR() {
    return this.buildData({ claimType: ClaimType.ONE_VS_TWO_LIP_LR });
  }

  async buildSmall1v2LRLIP() {
    return this.buildData({ claimType: ClaimType.ONE_VS_TWO_LR_LIP });
  }

  protected async buildData({
    claimType = ClaimType.ONE_VS_ONE,
    claimTypeUnspec = {
      claimTypeUnspec: ClaimTypeUnspec.PERSONAL_INJURY,
      personalInjuryType: PersonalInjuryType.ROAD_ACCIDENT,
    },
    claimTrack = ClaimTrack.SMALL_CLAIM,
    claimant1PartyType = claimantDefendantPartyTypes.INDIVIDUAL,
    claimant2PartyType = claimantDefendantPartyTypes.INDIVIDUAL,
    defendant1PartyType = claimantDefendantPartyTypes.INDIVIDUAL,
    defendant2PartyType = claimantDefendantPartyTypes.INDIVIDUAL,
  }: {
    claimType?: ClaimType;
    claimTypeUnspec?: PersonalInjuryClaimTypeUnspecObjs | ClaimTypeUnspec;
    claimTrack?: ClaimTrack;
    claimant1PartyType?: ClaimantDefendantPartyType;
    claimant2PartyType?: ClaimantDefendantPartyType;
    defendant1PartyType?: ClaimantDefendantPartyType;
    defendant2PartyType?: ClaimantDefendantPartyType;
  } = {}) {
    const { civilServiceRequests } = this.requestsFactory;
    this.setClaimantDefendantPartyTypes(claimType, {
      claimant1PartyType,
      claimant2PartyType,
      defendant1PartyType,
      defendant2PartyType,
    });

    const certificateOfSuitability =
      await civilServiceRequests.uploadTestDocument(claimantSolicitorUser);
    let certificateOfSuitability2: UploadDocumentValue;
    if (ClaimTypeHelper.isClaimant2(claimType)) {
      certificateOfSuitability2 =
        await civilServiceRequests.uploadTestDocument(claimantSolicitorUser);
    }

    return {
      ...createClaimData.references,
      ...createClaimData.claimantCourt,
      ...createClaimData.claimant1(claimant1PartyType, certificateOfSuitability),
      ...createClaimData.claimantSolicitor1,
      ...createClaimData.claimant2(claimType, claimant2PartyType, certificateOfSuitability2!),
      ...createClaimData.defendant1(defendant1PartyType),
      ...createClaimData.defendantSolicitor1(claimType),
      ...createClaimData.defendant2(claimType, defendant2PartyType),
      ...createClaimData.defendant2Represented(claimType),
      ...createClaimData.defendant2SameSolicitor(claimType),
      ...createClaimData.defendantSolicitor2(claimType),
      ...createClaimData.claimTypeUnspec(claimTypeUnspec),
      ...createClaimData.claimDetails(claimTrack),
      ...createClaimData.statementOfTruth,
    };
  }
}
