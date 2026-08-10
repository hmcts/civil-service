import { z } from 'zod';
import BaseSchemaBuilder from '../../../../../base/base-schema-builder';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import ZodHelper from '../../../../../helpers/zod-helper';
import CCDCaseData from '../../../../../models/ccd-case-data';
import uploadMediationDocumentsSchemaComponents from './upload-mediation-documents-schema-components';
import partys from '../../../../../constants/users/partys';
import { Party } from '../../../../../models/users/partys';

@AllMethodsStep({ methodNamesToIgnore: ['buildSchema'] })
export default class UploadMediationDocumentsSchemaBuilder extends BaseSchemaBuilder {
  async buildCS1(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission);
  }

  async buildCS12v1(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission);
  }

  async buildDS1(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {
      claimantDefendantSolicitorParty: partys.DEFENDANT_SOLICITOR_1,
    });
  }

  async buildDS2(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {
      claimantDefendantSolicitorParty: partys.DEFENDANT_SOLICITOR_2,
    });
  }

  protected async buildSchema(caseDataBeforeSubmission?: CCDCaseData, {
    claimantDefendantSolicitorParty = partys.CLAIMANT_SOLICITOR_1,
  }: {claimantDefendantSolicitorParty?: Party} = {}): Promise<z.ZodType> {
    const baseSchema = ZodHelper.createSchemaFromJson(caseDataBeforeSubmission, {
      strictObjects: false,
    }) as z.ZodObject<any>;

    return baseSchema.extend({
      ...uploadMediationDocumentsSchemaComponents.mediationDocumentsReferred(
        claimantDefendantSolicitorParty,
      ),
      ...uploadMediationDocumentsSchemaComponents.mediationNonAttendanceDocs(
        claimantDefendantSolicitorParty,
      ),
    });
  }
}
