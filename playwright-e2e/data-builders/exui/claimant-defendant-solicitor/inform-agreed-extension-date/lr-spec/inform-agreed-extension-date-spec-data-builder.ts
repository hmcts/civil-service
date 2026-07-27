import BaseDataBuilder from '../../../../../base/base-data-builder';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import informAgreedExtensionDateSpecData from './inform-agreed-extension-date-spec-data-components';

@AllMethodsStep({ methodNamesToIgnore: ['buildData'] })
export default class InformAgreedExtensionDateSpecDataBuilder extends BaseDataBuilder {
  async buildDataDS1() {
    return this.buildData();
  }

  protected async buildData() {
    return {
      ...informAgreedExtensionDateSpecData.extensionDate(this.ccdCaseData),
    };
  }
}
