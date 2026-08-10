import BaseDataBuilder from '../../../../base/base-data-builder';
import NotSuitableSdoOption from '../../../../constants/ccd-events/non-suitable-sdo/not-suitable-sdo-option';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import notSuitableSdoDataBuilderComponents from './not-suitable-sdo-data-builder-components';

@AllMethodsStep({ methodNamesToIgnore: ['buildData'] })
export default class NotSuitableSdoDataBuilder extends BaseDataBuilder {
  async buildChangeLocation() {
    return this.buildData();
  }

  async buildOtherReasons() {
    return this.buildData({notSuitableSdoOption: NotSuitableSdoOption.OTHER_REASONS});
  }

  protected async buildData({
    notSuitableSdoOption = NotSuitableSdoOption.CHANGE_LOCATION,
  }: {
    notSuitableSdoOption?: NotSuitableSdoOption;
  } = {}) {
    return {
      ...notSuitableSdoDataBuilderComponents.notSuitableSdo(notSuitableSdoOption),
    };
  }
}
