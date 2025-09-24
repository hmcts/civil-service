import { Page } from '@playwright/test';
import BasePage from '../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import ExuiPage from '../../../../exui-page/exui-page';
import AddressFragment from '../../../../fragments/address/address-fragment.ts';
import ChoosePartyTypeFragment from '../../../../fragments/choose-party-type/choose-party-type-fragment.ts';
import PartyTypeCompanyFragment from '../../../../fragments/party-type/party-type-company-fragment.ts';
import PartyTypeIndividualFragment from '../../../../fragments/party-type/party-type-individual-fragment.ts';
import PartyTypeOrganisationFragment from '../../../../fragments/party-type/party-type-organisation-fragment.ts';
import PartyTypeSoleTraderFragment from '../../../../fragments/party-type/party-type-sole-trader-fragment.ts';
import { radioButtons, subheadings } from './second-claimant-content.ts';
import partys from '../../../../../../constants/partys.ts';

@AllMethodsStep()
export default class SecondClaimantPage extends ExuiPage(BasePage) {
  private choosePartyTypeFragment: ChoosePartyTypeFragment;
  private partyTypeIndividualFragment: PartyTypeIndividualFragment;
  private partyTypeCompanyFragment: PartyTypeCompanyFragment;
  private partyTypeOrganisationFragment: PartyTypeOrganisationFragment;
  private partyTypeSoleTraderFragment: PartyTypeSoleTraderFragment;
  private addressFragment: AddressFragment;

  constructor(page: Page) {
    super(page);
    this.choosePartyTypeFragment = new ChoosePartyTypeFragment(page, partys.CLAIMANT_2);
    this.partyTypeIndividualFragment = new PartyTypeIndividualFragment(page, partys.CLAIMANT_2);
    this.partyTypeCompanyFragment = new PartyTypeCompanyFragment(page, partys.CLAIMANT_2);
    this.partyTypeOrganisationFragment = new PartyTypeOrganisationFragment(page, partys.CLAIMANT_2);
    this.partyTypeSoleTraderFragment = new PartyTypeSoleTraderFragment(page, partys.CLAIMANT_2);
    this.addressFragment = new AddressFragment(page, partys.CLAIMANT_2);
  }

  async verifyContent() {
    await super.runVerifications([
      super.verifyHeadings(),
      this.choosePartyTypeFragment.verifyContent(),
      super.expectSubheading(subheadings.claimantDetails),
      super.expectLegend(radioButtons.partyType.label),
      super.expectSubheading(subheadings.address),
    ]);
  }

  async chooseIndividualAndEnterDetails() {
    await this.choosePartyTypeFragment.selectIndivdual();
    await this.partyTypeIndividualFragment.verifyContent();
    await this.partyTypeIndividualFragment.enterIndividualDetails();
    await this.addressFragment.enterAddressManual();
  }

  async chooseCompanyAndEnterDetails() {
    await this.choosePartyTypeFragment.selectCompany();
    await this.partyTypeCompanyFragment.verifyContent();
    await this.partyTypeCompanyFragment.enterCompanyDetails();
    await this.addressFragment.enterAddressManual();
  }

  async chooseOrganisationAndEnterDetails() {
    await this.choosePartyTypeFragment.selectOrganisation();
    await this.partyTypeOrganisationFragment.verifyContent();
    await this.partyTypeOrganisationFragment.enterOrganisationDetails();
    await this.addressFragment.enterAddressManual();
  }

  async chooseSoleTraderAndEnterDetails() {
    await this.choosePartyTypeFragment.selectSoleTrader();
    await this.partyTypeSoleTraderFragment.verifyContent();
    await this.partyTypeSoleTraderFragment.enterSoleTraderDetails();
    await this.addressFragment.enterAddressManual();
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
