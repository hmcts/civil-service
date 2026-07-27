import { z } from 'zod';
import BaseSchemaBuilder from '../../../../../base/base-schema-builder';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import ZodHelper from '../../../../../helpers/zod-helper';
import CCDCaseData from '../../../../../models/ccd-case-data';
import evidenceUploadRespondentSchemaComponents from './evidence-upload-respondent-schema-components';
import ClaimTrack from '../../../../../constants/cases/claim-track';

@AllMethodsStep()
export default class EvidenceUploadRespondentSchemaBuilder extends BaseSchemaBuilder {
  async buildDS1Fast(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {claimTrack: ClaimTrack.FAST_CLAIM})
  }

  async buildDS1Fast1v2SS(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {claimTrack: ClaimTrack.FAST_CLAIM})
  }

  async buildDS2Fast1v2SS(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {claimTrack: ClaimTrack.FAST_CLAIM})
  }

  async buildDS1SmallClaim(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission)
  }

  protected async buildSchema(caseDataBeforeSubmission?: CCDCaseData, 
    {
    claimTrack = ClaimTrack.SMALL_CLAIM
  }: {
    claimTrack?: ClaimTrack
  } = {}): Promise<z.ZodType> {
    const baseSchema = ZodHelper.createSchemaFromJson(caseDataBeforeSubmission, {
      strictObjects: false,
    }) as z.ZodObject<any>;

    return baseSchema.extend({
      ...evidenceUploadRespondentSchemaComponents.documentUpload(claimTrack),
    });
  }
}
