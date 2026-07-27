import BaseDataBuilder from '../../../../base/base-data-builder';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import mediationUnsuccessfulDataBuilderComponents from './mediation-unsuccessful-data-builder-components';

@AllMethodsStep({ methodNamesToIgnore: ['buildData'] })
export default class MediationUnsuccessfulDataBuilder extends BaseDataBuilder {
  async buildData(
  ) {
    return {
      ...mediationUnsuccessfulDataBuilderComponents.mediationUnsuccessful,
      ...mediationUnsuccessfulDataBuilderComponents.workAllocationIntegratedFields,
    };
  }
}
