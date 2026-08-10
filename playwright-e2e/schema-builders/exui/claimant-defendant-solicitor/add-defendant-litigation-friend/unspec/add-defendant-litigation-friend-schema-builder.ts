import { z } from 'zod';
import BaseSchemaBuilder from '../../../../../base/base-schema-builder';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import ZodHelper from '../../../../../helpers/zod-helper';
import CCDCaseData from '../../../../../models/ccd-case-data';
import addDefendantLitigationFriendSchemaComponents from './add-defendant-litigation-friend-schema-components';

@AllMethodsStep({ methodNamesToIgnore: ['buildSchema'] })
export default class AddDefendantLitigationFriendSchemaBuilder extends BaseSchemaBuilder {
  async buildSchemaDS1(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission);
  }

  protected async buildSchema(caseDataBeforeSubmission?: CCDCaseData): Promise<z.ZodType> {
    const baseSchema = ZodHelper.createSchemaFromJson(caseDataBeforeSubmission, {
      strictObjects: false,
    }) as z.ZodObject<any>;

    return baseSchema.extend({
      ...addDefendantLitigationFriendSchemaComponents.respondent1LitigationFriend,
    });
  }
}
