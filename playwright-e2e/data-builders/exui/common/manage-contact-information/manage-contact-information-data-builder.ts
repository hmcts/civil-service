import BaseDataBuilder from '../../../../base/base-data-builder';
import partys from '../../../../constants/users/partys';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import { Party } from '../../../../models/users/partys';
import manageContactInformationDataComponents from './manage-contact-information-data-builder-components';

@AllMethodsStep({ methodNamesToIgnore: ['buildData'] })
export default class ManageContactInformationDataBuilder extends BaseDataBuilder {
  async buildClaimant() {
    return this.buildData();
  }

  async buildDS1LegalRepresentation() {
    return this.buildData({ party: partys.DEFENDANT_SOLICITOR_1 });
  }

  protected async buildData({
    party = partys.CLAIMANT_1,
  }: {
    party?: Party
  } = {}) {
    return {
      ...manageContactInformationDataComponents.partySelection(party, this.claimant1PartyType!),
      ...manageContactInformationDataComponents.claimant1Party(party, this.claimant1PartyType!),
      ...manageContactInformationDataComponents.legalRepresentativeIndividuals(party),
    };
  }
}
