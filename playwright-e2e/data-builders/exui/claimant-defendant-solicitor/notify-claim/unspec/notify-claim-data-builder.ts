import BaseDataBuilder from '../../../../../base/base-data-builder';
import ClaimType from '../../../../../constants/cases/claim-type';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import notifyClaimDataBuilderComponents from './notify-claim-data-builder-components';

@AllMethodsStep()
export default class NotifyClaimDataBuilder extends BaseDataBuilder {
  async build() {
    return this.buildData();
  }

  async build1vLIP() {
    return this.buildData({claimType: ClaimType.ONE_VS_ONE_LIP})
  }

  async build1vLRLIP() {
    return this.buildData({claimType: ClaimType.ONE_VS_TWO_LR_LIP})
  }

  async build1v2LRLIP() {
    return this.buildData({claimType: ClaimType.ONE_VS_TWO_LR_LIP})
  }

  async build1v2LIPS() {
    return this.buildData({claimType: ClaimType.ONE_VS_TWO_LIPS})
  }

  protected async buildData(
    {
      claimType = ClaimType.ONE_VS_ONE
    } :
    {
      claimType?: ClaimType
    } = {}) {
    return {
      ...notifyClaimDataBuilderComponents.selectDefendantSolicitorToNotify,
      ...notifyClaimDataBuilderComponents.accessGrantedWarning(claimType),
      ...notifyClaimDataBuilderComponents.certificateOfService1(claimType),
      ...notifyClaimDataBuilderComponents.certificateOfService2(claimType)
    };
  }
}
