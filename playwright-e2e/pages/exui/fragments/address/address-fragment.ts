import { Page } from '@playwright/test';
import BasePage from '../../../../base/base-page';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import ExuiPage from '../../exui-page/exui-page';
import { buttons, inputs, dropdowns, links } from './address-content';
import { Party } from '../../../../models/partys';
import CaseDataHelper from '../../../../helpers/case-data-helper';

@AllMethodsStep()
export default class AddressFragment extends ExuiPage(BasePage) {
  private party: Party;

  constructor(page: Page, party: Party) {
    super(page);
    this.party = party;
  }

  async verifyContent() {
    await super.runVerifications([super.expectLabel(inputs.postCodeInput.label)]);
  }

  async enterAddressManual() {
    const addressData = CaseDataHelper.buildAddressData(this.party);
    await super.clickLink(links.cannotFindAddress.title);
    await super.inputText(addressData.AddressLine1, inputs.addressLine1.selector(this.party));
    await super.inputText(addressData.AddressLine2, inputs.addressLine2.selector(this.party));
    await super.inputText(addressData.AddressLine3, inputs.addressLine3.selector(this.party));
    await super.inputText(addressData.PostTown, inputs.postTown.selector(this.party));
    await super.inputText(addressData.County, inputs.county.selector(this.party));
    await super.inputText(addressData.Country, inputs.country.selector(this.party));
    await super.inputText(addressData.PostCode, inputs.postCode.selector(this.party));
  }

  async findAddress(postcode: string, index: number) {
    await super.inputText(postcode, inputs.postCodeInput.selector(this.party));
    await super.clickButtonByName(buttons.findaddress.title);
    await super.expectSelector(dropdowns.addressList.selector(this.party));
    await super.selectFromDropdown(index, dropdowns.addressList.selector(this.party));
  }

  async submit() {
    throw new Error('Method not implemented.');
  }
}
