import BasePage from '../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../decorators/test-steps.ts';
import { subheadings } from './defendant-1-party-content.ts';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page.ts';
import CCDCaseData from '../../../../../models/ccd-case-data.ts';
import PartyTypeIndividualFragment from '../../../fragments/party-type/party-type-individual-fragment.ts';
import PartyTypeCompanyFragment from '../../../fragments/party-type/party-type-company-fragment.ts';
import PartyTypeOrganisationFragment from '../../../fragments/party-type/party-type-organisation-fragment.ts';
import PartyTypeSoleTraderFragment from '../../../fragments/party-type/party-type-sole-trader-fragment.ts';
import { Page } from '@playwright/test';
import partys from '../../../../../constants/users/partys.ts';
import AddressFragment from '../../../fragments/address/address-fragment.ts';
import { ClaimantDefendantPartyType } from '../../../../../models/users/claimant-defendant-party-types.ts';
import claimantDefendantPartyTypes from '../../../../../constants/users/claimant-defendant-party-types.ts';

@AllMethodsStep()
export default class Defendant1PartyPage extends ExuiPage(BasePage) {
  private partyTypeIndividualFragment: PartyTypeIndividualFragment;
  private partyTypeCompanyFragment: PartyTypeCompanyFragment;
  private partyTypeOrganisationFragment: PartyTypeOrganisationFragment;
  private partyTypeSoleTraderFragment: PartyTypeSoleTraderFragment;
  private addressFragment: AddressFragment;

  constructor(page: Page) {
    super(page);
    this.partyTypeIndividualFragment = new PartyTypeIndividualFragment(page, partys.DEFENDANT_1);
    this.partyTypeCompanyFragment = new PartyTypeCompanyFragment(page, partys.DEFENDANT_1);
    this.partyTypeOrganisationFragment = new PartyTypeOrganisationFragment(
      page,
      partys.DEFENDANT_1,
    );
    this.partyTypeSoleTraderFragment = new PartyTypeSoleTraderFragment(page, partys.DEFENDANT_1);
    this.addressFragment = new AddressFragment(page, partys.DEFENDANT_1);
  }

  async verifyContent(ccdCaseData: CCDCaseData, defendant1PartyType: ClaimantDefendantPartyType) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectText(subheadings.party(partys.DEFENDANT_1, defendant1PartyType)),
    ]);
  }

  async updateDefendant1Details(defendant1PartyType: ClaimantDefendantPartyType) {
    switch (defendant1PartyType.type) {
      case claimantDefendantPartyTypes.INDIVIDUAL.type:
        await this.partyTypeIndividualFragment.verifyContent();
        await this.partyTypeIndividualFragment.enterIndividualDetails(true);
        break;
      case claimantDefendantPartyTypes.COMPANY.type:
        await this.partyTypeCompanyFragment.verifyContent();
        await this.partyTypeCompanyFragment.enterCompanyDetails(true);
        break;
      case claimantDefendantPartyTypes.ORGANISATION.type:
        await this.partyTypeOrganisationFragment.verifyContent();
        await this.partyTypeOrganisationFragment.enterOrganisationDetails(true);
        break;
      case claimantDefendantPartyTypes.SOLE_TRADER.type:
        await this.partyTypeSoleTraderFragment.verifyContent();
        await this.partyTypeSoleTraderFragment.enterSoleTraderDetails(true);
        break;
      default:
        throw new Error(`Unsupported defendant party type: ${defendant1PartyType.type}`);
    }

    await this.addressFragment.enterAddressManual(true);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
