import BaseDataBuilder from '../../../../../base/base-data-builder';
import claimantDefendantPartyTypes from '../../../../../constants/users/claimant-defendant-party-types';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import ClaimTrack from '../../../../../constants/cases/claim-track';
import ClaimType from '../../../../../constants/cases/claim-type';
import { ClaimantDefendantPartyType } from '../../../../../models/users/claimant-defendant-party-types';
import createClaimSpecData from './create-claim-spec-data-components';

@AllMethodsStep({ methodNamesToIgnore: ['buildData'] })
export default class CreateClaimSpecDataBuilder extends BaseDataBuilder {
  async buildFast1v1() {
    return this.buildData({ claimTrack: ClaimTrack.FAST_CLAIM });
  }

  async buildFast2v1() {
    return this.buildData({ claimTrack: ClaimTrack.FAST_CLAIM, claimType: ClaimType.TWO_VS_ONE });
  }

  async buildFast1v2SS() {
    return this.buildData({
      claimTrack: ClaimTrack.FAST_CLAIM,
      claimType: ClaimType.ONE_VS_TWO_SAME_SOL,
    });
  }

  async buildFast1v2DS() {
    return this.buildData({
      claimTrack: ClaimTrack.FAST_CLAIM,
      claimType: ClaimType.ONE_VS_TWO_DIFF_SOL,
    });
  }

  async buildIntermediate1v1() {
    return this.buildData({ claimTrack: ClaimTrack.INTERMEDIATE_CLAIM });
  }

  async buildIntermediate1v2SS() {
    return this.buildData({
      claimTrack: ClaimTrack.INTERMEDIATE_CLAIM,
      claimType: ClaimType.ONE_VS_TWO_SAME_SOL,
    });
  }

  async buildIntermediate1v2DS() {
    return this.buildData({
      claimTrack: ClaimTrack.INTERMEDIATE_CLAIM,
      claimType: ClaimType.ONE_VS_TWO_DIFF_SOL,
    });
  }

  async buildMulti1v1() {
    return this.buildData({ claimTrack: ClaimTrack.MULTI_CLAIM });
  }

  async buildMulti2v1() {
    return this.buildData({
      claimTrack: ClaimTrack.MULTI_CLAIM,
      claimType: ClaimType.TWO_VS_ONE,
    });
  }

  async buildMulti1v2SS() {
    return this.buildData({
      claimTrack: ClaimTrack.MULTI_CLAIM,
      claimType: ClaimType.ONE_VS_TWO_SAME_SOL,
    });
  }

  async buildMulti1v2DS() {
    return this.buildData({
      claimTrack: ClaimTrack.MULTI_CLAIM,
      claimType: ClaimType.ONE_VS_TWO_DIFF_SOL,
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
    claimTrack = ClaimTrack.SMALL_CLAIM,
    claimant1PartyType = claimantDefendantPartyTypes.INDIVIDUAL,
    claimant2PartyType = claimantDefendantPartyTypes.INDIVIDUAL,
    defendant1PartyType = claimantDefendantPartyTypes.INDIVIDUAL,
    defendant2PartyType = claimantDefendantPartyTypes.INDIVIDUAL,
  }: {
    claimType?: ClaimType;
    claimTrack?: ClaimTrack;
    claimant1PartyType?: ClaimantDefendantPartyType;
    claimant2PartyType?: ClaimantDefendantPartyType;
    defendant1PartyType?: ClaimantDefendantPartyType;
    defendant2PartyType?: ClaimantDefendantPartyType;
  } = {}) {
    this.setClaimantDefendantPartyTypes(claimType, {
      claimant1PartyType,
      claimant2PartyType,
      defendant1PartyType,
      defendant2PartyType,
    });

    return {
      ...createClaimSpecData.references,
      ...createClaimSpecData.claimant1(claimant1PartyType),
      ...createClaimSpecData.claimantSolicitor1,
      ...createClaimSpecData.claimant2(claimType, claimant2PartyType),
      ...createClaimSpecData.defendant1(defendant1PartyType),
      ...createClaimSpecData.defendantSolicitor1(claimType),
      ...createClaimSpecData.defendant2(claimType, defendant2PartyType),
      ...createClaimSpecData.defendant2Represented(claimType),
      ...createClaimSpecData.defendant2SameSolicitor(claimType),
      ...createClaimSpecData.defendantSolicitor2(claimType),
      ...createClaimSpecData.claimDetails(claimTrack),
      ...createClaimSpecData.statementOfTruth,
    };
  }
}
