import BaseDataBuilder from '../../../../../base/base-data-builder';
import partys from '../../../../../constants/users/partys';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import { Party } from '../../../../../models/users/partys';
import informAgreedExtensionDateDataComponents from './inform-agreed-extension-date-data-components';

@AllMethodsStep({ methodNamesToIgnore: ['buildData'] })
export default class InformAgreedExtensionDateDataBuilder extends BaseDataBuilder {
  async buildDataDS1() {
    return this.buildData({ defendantSolicitorParty: partys.DEFENDANT_SOLICITOR_1 });
  }

  async buildDataDS2() {
    return this.buildData({ defendantSolicitorParty: partys.DEFENDANT_SOLICITOR_2 });
  }

  protected async buildData({
    defendantSolicitorParty = partys.DEFENDANT_SOLICITOR_1,
  }: {
    defendantSolicitorParty?: Party
  } = {}) {
    return {
      ...informAgreedExtensionDateDataComponents.extensionDate(this.ccdCaseData, defendantSolicitorParty),
    };
  }
}
