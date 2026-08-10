import BaseDataBuilder from '../../../../base/base-data-builder';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import transferOnlineCaseDataBuilderComponents from './transfer-online-case-data-builder-components';

@AllMethodsStep({ methodNamesToIgnore: ['buildData'] })
export default class TransferOnlineCaseDataBuilder extends BaseDataBuilder {
  async buildData() {
    return {
      ...transferOnlineCaseDataBuilderComponents.transferOnlineCase,
    };
  }
}
