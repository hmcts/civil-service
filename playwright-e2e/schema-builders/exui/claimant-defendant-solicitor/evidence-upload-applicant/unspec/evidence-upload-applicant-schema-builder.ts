import { z } from 'zod';
import BaseSchemaBuilder from '../../../../../base/base-schema-builder';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import ZodHelper from '../../../../../helpers/zod-helper';
import CCDCaseData from '../../../../../models/ccd-case-data';
import evidenceUploadApplicantSchemaComponents from './evidence-upload-applicant-schema-components';
import ClaimTrack from '../../../../../constants/cases/claim-track';

@AllMethodsStep()
export default class EvidenceUploadApplicantSchemaBuilder extends BaseSchemaBuilder {
  async buildFast(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {claimTrack: ClaimTrack.FAST_CLAIM})
  }

  async buildFast2v1(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {claimTrack: ClaimTrack.FAST_CLAIM})
  }

  async buildSmallClaim(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission);
  }

  protected async buildSchema(caseDataBeforeSubmission?: CCDCaseData, {
    claimTrack = ClaimTrack.SMALL_CLAIM
  }: {
    claimTrack?: ClaimTrack
  } = {}): Promise<z.ZodType> {
    const baseSchema = ZodHelper.createSchemaFromJson(caseDataBeforeSubmission, {
      strictObjects: false,
    }) as z.ZodObject<any>;

    return baseSchema.extend({
      ...evidenceUploadApplicantSchemaComponents.documentUpload(claimTrack),
    });
  }
}
