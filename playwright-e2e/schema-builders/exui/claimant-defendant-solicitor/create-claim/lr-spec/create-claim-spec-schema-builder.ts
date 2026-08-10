import { z } from 'zod';
import BaseSchemaBuilder from '../../../../../base/base-schema-builder';
import claimantDefendantPartyTypes from '../../../../../constants/users/claimant-defendant-party-types';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import ClaimType from '../../../../../constants/cases/claim-type';
import { ClaimantDefendantPartyType } from '../../../../../models/users/claimant-defendant-party-types';
import createClaimSpecSchemaComponents from './create-claim-spec-schema-components';

@AllMethodsStep({ methodNamesToIgnore: ['buildSchema'] })
export default class CreateClaimSpecSchemaBuilder extends BaseSchemaBuilder {
  async buildFast1v1(): Promise<z.ZodType> {
    return this.buildSchema();
  }

  async buildFast2v1(): Promise<z.ZodType> {
    return this.buildSchema({ claimType: ClaimType.TWO_VS_ONE });
  }

  async buildFast1v2SS(): Promise<z.ZodType> {
    return this.buildSchema({ claimType: ClaimType.ONE_VS_TWO_SAME_SOL });
  }

  async buildFast1v2DS(): Promise<z.ZodType> {
    return this.buildSchema({ claimType: ClaimType.ONE_VS_TWO_DIFF_SOL });
  }

  async buildIntermediate1v1(): Promise<z.ZodType> {
    return this.buildSchema();
  }

  async buildIntermediate1v2SS(): Promise<z.ZodType> {
    return this.buildSchema({ claimType: ClaimType.ONE_VS_TWO_SAME_SOL });
  }

  async buildIntermediate1v2DS(): Promise<z.ZodType> {
    return this.buildSchema({ claimType: ClaimType.ONE_VS_TWO_DIFF_SOL });
  }

  async buildMulti1v1(): Promise<z.ZodType> {
    return this.buildSchema();
  }

  async buildMulti2v1(): Promise<z.ZodType> {
    return this.buildSchema({ claimType: ClaimType.TWO_VS_ONE });
  }

  async buildMulti1v2SS(): Promise<z.ZodType> {
    return this.buildSchema({ claimType: ClaimType.ONE_VS_TWO_SAME_SOL });
  }

  async buildMulti1v2DS(): Promise<z.ZodType> {
    return this.buildSchema({ claimType: ClaimType.ONE_VS_TWO_DIFF_SOL });
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
    claimant1PartyType = claimantDefendantPartyTypes.INDIVIDUAL,
    claimant2PartyType = claimantDefendantPartyTypes.INDIVIDUAL,
    defendant1PartyType = claimantDefendantPartyTypes.INDIVIDUAL,
    defendant2PartyType = claimantDefendantPartyTypes.INDIVIDUAL,
  }: {
    claimType?: ClaimType;
    claimant1PartyType?: ClaimantDefendantPartyType;
    claimant2PartyType?: ClaimantDefendantPartyType;
    defendant1PartyType?: ClaimantDefendantPartyType;
    defendant2PartyType?: ClaimantDefendantPartyType;
  } = {}): Promise<z.ZodType> {
    const schemaShape: Record<string, z.ZodType> = {};

    Object.assign(
      schemaShape,
      createClaimSpecSchemaComponents.references,
      createClaimSpecSchemaComponents.claimantCourt,
      createClaimSpecSchemaComponents.claimant1(claimant1PartyType),
      createClaimSpecSchemaComponents.claimantSolicitor1,
      createClaimSpecSchemaComponents.defendant1(defendant1PartyType),
      createClaimSpecSchemaComponents.statementOfTruth,
      createClaimSpecSchemaComponents.solicitorReferences(claimType),
      createClaimSpecSchemaComponents.claimDetails(),
      createClaimSpecSchemaComponents.claimant2(claimType, claimant2PartyType),
      createClaimSpecSchemaComponents.defendantSolicitor1(claimType),
      createClaimSpecSchemaComponents.defendant2(claimType, defendant2PartyType),
      createClaimSpecSchemaComponents.defendant2Representation(claimType),
    );

    return z.looseObject(schemaShape);
  }
}
