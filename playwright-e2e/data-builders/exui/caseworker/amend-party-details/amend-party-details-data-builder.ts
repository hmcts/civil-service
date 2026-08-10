import BaseDataBuilder from '../../../../base/base-data-builder';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import amendPartyDetailsDataBuilderComponents from './amend-party-details-data-builder-components';

@AllMethodsStep({ methodNamesToIgnore: ['buildData'] })
export default class AmendPartyDetailsDataBuilder extends BaseDataBuilder {
  async buildData() {
    return {
      ...amendPartyDetailsDataBuilderComponents.email,
    };
  }
}
