import BaseDataBuilder from '../../../../../base/base-data-builder';
import { claimantSolicitorUser } from '../../../../../config/users/exui-users';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import addOrAmendClaimDocumentsDataBuilderComponents from './add-or-amend-claim-documents-data-builder-components';

@AllMethodsStep({ methodNamesToIgnore: ['buildData'] })
export default class AddOrAmendClaimDocumentsDataBuilder extends BaseDataBuilder {
  async buildData() {
    const { civilServiceRequests } = this.requestsFactory;
    const particularsOfClaimDocument =
      await civilServiceRequests.uploadTestDocument(claimantSolicitorUser);

    return {
      ...addOrAmendClaimDocumentsDataBuilderComponents.upload(particularsOfClaimDocument),
    };
  }
}
