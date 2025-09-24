import BaseDataBuilder from '../../../../../base/base-data-builder';
import claimantDefendantPartyTypes from '../../../../../constants/claimant-defendant-party-types';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import ClaimTrack from '../../../../../enums/claim-track';
import ClaimType from '../../../../../enums/claim-type';
import { ClaimantDefendantPartyType } from '../../../../../models/claimant-defendant-party-types';
import createClaimSpecData from './create-claim-spec-data-components';

@AllMethodsStep({ methodNamesToIgnore: ['buildData'] })
export default class CreateClaimSpecDataBuilder extends BaseDataBuilder {
  async buildFastTrack1v1() {
    return this.buildData({ claimTrack: ClaimTrack.FAST_CLAIM });
  }

  async buildSmallTrack1v1() {
    return this.buildData();
  }

  async buildSmallTrack2v1() {
    return this.buildData({ claimType: ClaimType.TWO_VS_ONE });
  }

  async buildSmallTrack1v2SS() {
    return this.buildData({ claimType: ClaimType.ONE_VS_TWO_SAME_SOL });
  }

  async buildSmallTrack1v2DS() {
    return this.buildData({ claimType: ClaimType.ONE_VS_TWO_DIFF_SOL });
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
