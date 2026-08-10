import { z } from 'zod';
import BaseSchemaBuilder from '../../../../../base/base-schema-builder';
import partys from '../../../../../constants/users/partys';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import ZodHelper from '../../../../../helpers/zod-helper';
import CCDCaseData from '../../../../../models/ccd-case-data';
import { Party } from '../../../../../models/users/partys';
import informAgreedExtensionDateSchemaComponents from './inform-agreed-extension-date-schema-components';

@AllMethodsStep({ methodNamesToIgnore: ['buildSchema'] })
export default class InformAgreedExtensionDateSchemaBuilder extends BaseSchemaBuilder {
  async buildSchemaDS1(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema({
      caseDataBeforeSubmission,
      defendantSolicitorParty: partys.DEFENDANT_SOLICITOR_1,
    });
  }

  async buildSchemaDS2(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema({
      caseDataBeforeSubmission,
      defendantSolicitorParty: partys.DEFENDANT_SOLICITOR_2,
    });
  }

  protected async buildSchema(
    {
      caseDataBeforeSubmission,
      defendantSolicitorParty = partys.DEFENDANT_SOLICITOR_1,
    }: {
      caseDataBeforeSubmission?: CCDCaseData;
      defendantSolicitorParty?: Party;
    } = {},
  ): Promise<z.ZodType> {
    const baseSchema = ZodHelper.createSchemaFromJson(caseDataBeforeSubmission, {
      strictObjects: false,
    }) as z.ZodObject<any>;

    return baseSchema.extend({
      ...informAgreedExtensionDateSchemaComponents.extensionDate(defendantSolicitorParty),
      ...informAgreedExtensionDateSchemaComponents.undefine,
    });
  }
}
