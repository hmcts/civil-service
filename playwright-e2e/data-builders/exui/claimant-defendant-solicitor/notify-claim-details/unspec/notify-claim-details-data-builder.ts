import BaseDataBuilder from '../../../../../base/base-data-builder';
import { claimantSolicitorUser } from '../../../../../config/users/exui-users';
import ClaimType from '../../../../../constants/cases/claim-type';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import ClaimTypeHelper from '../../../../../helpers/claim-type-helper';
import notifyClaimDetailsDataBuilderComponents from './notify-claim-details-data-builder-components';

@AllMethodsStep()
export default class NotifyClaimDetailsDataBuilder extends BaseDataBuilder {
  async build() {
    return this.buildData();
  }

  async build1vLIP() {
    return this.buildData({ claimType: ClaimType.ONE_VS_ONE_LIP });
  }

  async build1v2LRLIP() {
    return this.buildData({ claimType: ClaimType.ONE_VS_TWO_LR_LIP });
  }

  async build1v2LIPS() {
    return this.buildData({ claimType: ClaimType.ONE_VS_TWO_LIPS });
  }

  protected async buildData({
      claimType = ClaimType.ONE_VS_ONE
    } :
    {
      claimType?: ClaimType
    } = {}) {
    let particularsOfClaimDocument;
    let defendant1SupportEvidenceCos;
    let defendant2SupportEvidenceCos;

    if (!this.ccdCaseData?.servedDocumentFiles?.particularsOfClaimDocument && (
      ClaimTypeHelper.isDefendant1Represented(claimType) ||
      ClaimTypeHelper.isDefendant2Represented(claimType)
    )) {
      const { civilServiceRequests } = this.requestsFactory;
      particularsOfClaimDocument =
        await civilServiceRequests.uploadTestDocument(claimantSolicitorUser);
    }

    if (ClaimTypeHelper.isDefendant1Unrepresented(claimType)) {
      const { civilServiceRequests } = this.requestsFactory;
      defendant1SupportEvidenceCos =
        await civilServiceRequests.uploadTestDocument(claimantSolicitorUser);
    }

    if (ClaimTypeHelper.isDefendant2Unrepresented(claimType)) {
      const { civilServiceRequests } = this.requestsFactory;
      defendant2SupportEvidenceCos =
        await civilServiceRequests.uploadTestDocument(claimantSolicitorUser);
    }

    return {
      ...notifyClaimDetailsDataBuilderComponents.selectDefendantSolicitor,
      ...notifyClaimDetailsDataBuilderComponents.upload(claimType, particularsOfClaimDocument!),
      ...notifyClaimDetailsDataBuilderComponents.certificateOfService1(claimType, defendant1SupportEvidenceCos!),
      ...notifyClaimDetailsDataBuilderComponents.certificateOfService2(claimType, defendant2SupportEvidenceCos!),
    };
  }
}
