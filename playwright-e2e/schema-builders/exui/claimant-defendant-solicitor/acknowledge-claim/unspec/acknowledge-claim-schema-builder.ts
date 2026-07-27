import { z } from 'zod';
import BaseSchemaBuilder from '../../../../../base/base-schema-builder';
import ClaimType from '../../../../../constants/cases/claim-type';
import partys from '../../../../../constants/users/partys';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import ZodHelper from '../../../../../helpers/zod-helper';
import CCDCaseData from '../../../../../models/ccd-case-data';
import { Party } from '../../../../../models/users/partys';
import acknowledgeClaimSchemaComponents from './acknowledge-claim-schema-components';

@AllMethodsStep({ methodNamesToIgnore: ['buildSchema'] })
export default class AcknowledgeClaimSchemaBuilder extends BaseSchemaBuilder {
  async buildSchemaDS1FullDefence(
    caseDataBeforeSubmission?: CCDCaseData,
  ) {
    return this.buildSchema({
      caseDataBeforeSubmission,
    });
  }

  async buildSchemaDS1FullDefence2v1(
    caseDataBeforeSubmission?: CCDCaseData,
  ) {
    return this.buildSchema({
      caseDataBeforeSubmission,
      claimType: ClaimType.TWO_VS_ONE,
    });
  }

  async buildSchemaDS1FullDefence1v2SS(
    caseDataBeforeSubmission?: CCDCaseData,
  ) {
    return this.buildSchema({
      caseDataBeforeSubmission,
      claimType: ClaimType.ONE_VS_TWO_SAME_SOL,
    });
  }

  async buildSchemaDS2FullDefence(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema({
      caseDataBeforeSubmission,
      claimType: ClaimType.ONE_VS_TWO_DIFF_SOL,
      defendantSolicitorParty: partys.DEFENDANT_SOLICITOR_2,
    });
  }


  protected async buildSchema(
    {
      caseDataBeforeSubmission,
      claimType = ClaimType.ONE_VS_ONE,
      defendantSolicitorParty = partys.DEFENDANT_SOLICITOR_1,
    }: {
      caseDataBeforeSubmission?: CCDCaseData;
      claimType?: ClaimType;
      defendantSolicitorParty?: Party;
    } = {},
  ): Promise<z.ZodType> {
    const baseSchema = ZodHelper.createSchemaFromJson(caseDataBeforeSubmission, {
      strictObjects: false,
    }) as z.ZodObject<any>;

    return baseSchema.extend({
      ...acknowledgeClaimSchemaComponents.responseIntention(claimType, defendantSolicitorParty),
      ...acknowledgeClaimSchemaComponents.responseDates(defendantSolicitorParty),
      ...acknowledgeClaimSchemaComponents.solicitorReferences(defendantSolicitorParty),
    });
  }
}
