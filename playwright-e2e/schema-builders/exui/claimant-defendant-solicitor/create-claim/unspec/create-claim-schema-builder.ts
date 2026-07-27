import { z } from 'zod';
import BaseSchemaBuilder from '../../../../../base/base-schema-builder';
import claimantDefendantPartyTypes from '../../../../../constants/users/claimant-defendant-party-types';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import ClaimTypeUnspec from '../../../../../constants/ccd-events/create-claim/claim-type-unspec';
import PersonalInjuryType from '../../../../../constants/ccd-events/create-claim/personal-injury-type';
import ClaimTrack from '../../../../../constants/cases/claim-track';
import ClaimType from '../../../../../constants/cases/claim-type';
import { ClaimantDefendantPartyType } from '../../../../../models/users/claimant-defendant-party-types';
import PersonalInjuryClaimTypeUnspecObjs from '../../../../../models/ccd-events/create-claim/claim-type-unspec-objs';
import createClaimResponseSchema from './create-claim-schema-components';

@AllMethodsStep({ methodNamesToIgnore: ['buildSchema'] })
export default class CreateClaimSchemaBuilder extends BaseSchemaBuilder {
  async buildFast1v1(): Promise<z.ZodType> {
    return this.buildSchema({ claimTrack: ClaimTrack.FAST_CLAIM });
  }

  async buildFastNIHL1v1(): Promise<z.ZodType> {
    return this.buildSchema({
      claimTrack: ClaimTrack.FAST_CLAIM,
      claimTypeUnspec: {
        claimTypeUnspec: ClaimTypeUnspec.PERSONAL_INJURY,
        personalInjuryType: PersonalInjuryType.NOISE_INDUCED_HEARING_LOSS,
      },
    });
  }

  async buildFast1v2DS(): Promise<z.ZodType> {
    return this.buildSchema({
      claimType: ClaimType.ONE_VS_TWO_DIFF_SOL,
      claimTrack: ClaimTrack.FAST_CLAIM,
    });
  }

  async buildFast1v2SS(): Promise<z.ZodType> {
    return this.buildSchema({
      claimType: ClaimType.ONE_VS_TWO_SAME_SOL,
      claimTrack: ClaimTrack.FAST_CLAIM,
    });
  }

  async buildFast2v1(): Promise<z.ZodType> {
    return this.buildSchema({
      claimType: ClaimType.TWO_VS_ONE,
      claimTrack: ClaimTrack.FAST_CLAIM,
    });
  }

  async buildIntermediate1v1(): Promise<z.ZodType> {
    return this.buildSchema({ claimTrack: ClaimTrack.INTERMEDIATE_CLAIM });
  }

  async buildIntermediate1v2SS(): Promise<z.ZodType> {
    return this.buildSchema({
      claimType: ClaimType.ONE_VS_TWO_SAME_SOL,
      claimTrack: ClaimTrack.INTERMEDIATE_CLAIM,
    });
  }

  async buildIntermediate2v1(): Promise<z.ZodType> {
    return this.buildSchema({
      claimType: ClaimType.TWO_VS_ONE,
      claimTrack: ClaimTrack.INTERMEDIATE_CLAIM,
    });
  }

  async buildMulti1v1(): Promise<z.ZodType> {
    return this.buildSchema({ claimTrack: ClaimTrack.MULTI_CLAIM });
  }

  async buildMulti2v1(): Promise<z.ZodType> {
    return this.buildSchema({
      claimType: ClaimType.TWO_VS_ONE,
      claimTrack: ClaimTrack.MULTI_CLAIM,
    });
  }

  async buildMulti1v2SS(): Promise<z.ZodType> {
    return this.buildSchema({
      claimType: ClaimType.ONE_VS_TWO_SAME_SOL,
      claimTrack: ClaimTrack.MULTI_CLAIM,
    });
  }

  async buildMulti1v2DS(): Promise<z.ZodType> {
    return this.buildSchema({
      claimType: ClaimType.ONE_VS_TWO_DIFF_SOL,
      claimTrack: ClaimTrack.MULTI_CLAIM,
    });
  }

  async buildSmall1v1(): Promise<z.ZodType> {
    return this.buildSchema();
  }

  async buildSmall2v1(): Promise<z.ZodType> {
    return this.buildSchema({ claimType: ClaimType.TWO_VS_ONE });
  }

  async buildSmall1v2SS(): Promise<z.ZodType> {
    return this.buildSchema({ claimType: ClaimType.ONE_VS_TWO_SAME_SOL });
  }

  async buildSmall1v2DS(): Promise<z.ZodType> {
    return this.buildSchema({ claimType: ClaimType.ONE_VS_TWO_DIFF_SOL });
  }

  async buildSmall1vLIP(): Promise<z.ZodType> {
    return this.buildSchema({ claimType: ClaimType.ONE_VS_ONE_LIP });
  }

  async buildSmall1v2LIPs(): Promise<z.ZodType> {
    return this.buildSchema({ claimType: ClaimType.ONE_VS_TWO_LIPS });
  }

  async buildSmall1v2LIPLR(): Promise<z.ZodType> {
    return this.buildSchema({ claimType: ClaimType.ONE_VS_TWO_LIP_LR });
  }

  async buildSmall1v2LRLIP(): Promise<z.ZodType> {
    return this.buildSchema({ claimType: ClaimType.ONE_VS_TWO_LR_LIP });
  }

  protected async buildSchema({
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
  } = {}): Promise<z.ZodType> {
    const schemaShape: Record<string, z.ZodType> = {};

    Object.assign(
      schemaShape,
      createClaimResponseSchema.references,
      createClaimResponseSchema.claimantCourt,
      createClaimResponseSchema.claimant1(claimant1PartyType),
      createClaimResponseSchema.claimantSolicitor1,
      createClaimResponseSchema.defendant1(defendant1PartyType),
      createClaimResponseSchema.statementOfTruth,
      createClaimResponseSchema.solicitorReferences(claimType),
      createClaimResponseSchema.claimDetails(claimTrack),
      createClaimResponseSchema.claimant2(claimType, claimant2PartyType),
      createClaimResponseSchema.defendantSolicitor1(claimType),
      createClaimResponseSchema.defendant2(claimType, defendant2PartyType),
      createClaimResponseSchema.defendant2Representation(claimType),
      createClaimResponseSchema.lipResponseArtifacts(claimType),
      createClaimResponseSchema.claimTypeUnspec(claimTypeUnspec),
    );

    return z.looseObject(schemaShape);
  }
}
