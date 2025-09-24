import CreateClaimSpecDataBuilder from './create-claim/lr-spec/create-claim-spec-data-builder';
import CreateClaimDataBuilder from './create-claim/unspec/create-claim-data-builder';
import ServiceRequestDataBuilder from './service-request/service-request-data-builder';

export default class ClaimantSolicitorDataBuilderFactory {
  get createClaimDataBuilder() {
    return new CreateClaimDataBuilder();
  }

  get createClaimSpecDataBuilder() {
    return new CreateClaimSpecDataBuilder();
  }

  get serviceRequestDataBuilder() {
    return new ServiceRequestDataBuilder();
  }
}
