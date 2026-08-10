import BaseDataBuilder from '../../../../base/base-data-builder';
import { judgeRegion1User } from '../../../../config/users/exui-users';
import ClaimTrack from '../../../../constants/cases/claim-track';
import OrderType from '../../../../constants/ccd-events/generate-directions-order/order-type';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import { UploadDocumentValue } from '../../../../models/ccd-case-data';
import generateDirectionsOrderDataBuilderComponents from './generate-directions-order-data-builder-components';

@AllMethodsStep({ methodNamesToIgnore: ['buildData'] })
export default class GenerateDirectionsOrderDataBuilder extends BaseDataBuilder {
  async buildAssistedOrder() {
    return this.buildData({claimTrack: ClaimTrack.FAST_CLAIM});
  }

  async buildFreeFormOrder() {
    return this.buildData({claimTrack: ClaimTrack.FAST_CLAIM, orderType: OrderType.FREE_FORM_ORDER})
  }

  async buildIntermediateOrder() {
    return this.buildData({ claimTrack: ClaimTrack.INTERMEDIATE_CLAIM });
  }

   async buildMultiOrder() {
    return this.buildData({ claimTrack: ClaimTrack.MULTI_CLAIM });
  }

  protected async buildData(
    {
      claimTrack = ClaimTrack.SMALL_CLAIM,
      orderType = OrderType.ASSISTED_ORDER ,
    } : {
      claimTrack?: ClaimTrack,
      orderType?: OrderType,
    } = {}) {
    const { civilServiceRequests } = this.requestsFactory;
    let templateDocument: UploadDocumentValue | undefined;
    if (claimTrack === ClaimTrack.INTERMEDIATE_CLAIM || claimTrack === ClaimTrack.MULTI_CLAIM) {
      templateDocument = await civilServiceRequests.uploadTestDocument(judgeRegion1User);
    }

    return {
      ...generateDirectionsOrderDataBuilderComponents.finalOrderSelect(claimTrack, orderType),
      ...generateDirectionsOrderDataBuilderComponents.finalOrderAssistedOrder(
        claimTrack,
        orderType,
        super.claimant1PartyType!,
        super.defendant1PartyType!,
      ),
      ...generateDirectionsOrderDataBuilderComponents.freeFormOrder(claimTrack,orderType),
      ...generateDirectionsOrderDataBuilderComponents.finalOrderPreview(claimTrack),
      ...generateDirectionsOrderDataBuilderComponents.trackAllocation(claimTrack),
      ...generateDirectionsOrderDataBuilderComponents.intermediateTrackComplexityBand(claimTrack),
      ...generateDirectionsOrderDataBuilderComponents.selectTemplate(claimTrack),
      ...generateDirectionsOrderDataBuilderComponents.orderAfterHearingDate(claimTrack),
      ...generateDirectionsOrderDataBuilderComponents.downloadTemplate(claimTrack),
      ...generateDirectionsOrderDataBuilderComponents.uploadOrder(claimTrack, templateDocument!),
    };
  }
}
