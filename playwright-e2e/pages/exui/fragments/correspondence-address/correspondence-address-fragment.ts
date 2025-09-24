import { Page } from '@playwright/test';
import BasePage from '../../../../base/base-page';
import CaseDataHelper from '../../../../helpers/case-data-helper';
import { Party } from '../../../../models/partys';
import ExuiPage from '../../exui-page/exui-page';
import {
  links,
  radioButtons,
  subheadings,
  inputs,
  buttons,
  dropdowns,
} from './correspondence-address-content';
import { AllMethodsStep } from '../../../../decorators/test-steps';

AllMethodsStep();
export default class CorrespondenceAddressFragment extends ExuiPage(BasePage) {
  private solicitorParty: Party;
  private claimantDefendantParty: Party;

  constructor(page: Page, solicitorParty: Party, claimantDefendantParty: Party) {
    super(page);
    this.solicitorParty = solicitorParty;
    this.claimantDefendantParty = claimantDefendantParty;
  }

  async verifyContent() {
    await super.runVerifications(
      [
        super.expectRadioYesLabel(
          radioButtons.addressRequired.yes.selector(this.claimantDefendantParty),
        ),
        super.expectRadioNoLabel(
          radioButtons.addressRequired.no.selector(this.claimantDefendantParty),
        ),
      ],
      {
        runAxe: false,
      },
    );
  }

  async selectYes() {
    await super.clickBySelector(
      radioButtons.addressRequired.yes.selector(this.claimantDefendantParty),
    );
    await super.expectSubheading(subheadings.correspondenceAddress);
  }

  async selectNo() {
    await super.clickBySelector(
      radioButtons.addressRequired.no.selector(this.claimantDefendantParty),
    );
  }

  async enterAddressManual() {
    const addressData = CaseDataHelper.buildAddressData(this.solicitorParty);
    await super.clickLink(links.cannotFindAddress.title);
    await super.inputText(
      addressData.AddressLine1,
      inputs.addressLine1.selector(this.claimantDefendantParty),
    );
    await super.inputText(
      addressData.AddressLine2,
      inputs.addressLine2.selector(this.claimantDefendantParty),
    );
    await super.inputText(
      addressData.AddressLine3,
      inputs.addressLine3.selector(this.claimantDefendantParty),
    );
    await super.inputText(
      addressData.PostTown,
      inputs.postTown.selector(this.claimantDefendantParty),
    );
    await super.inputText(addressData.County, inputs.county.selector(this.claimantDefendantParty));
    await super.inputText(
      addressData.Country,
      inputs.country.selector(this.claimantDefendantParty),
    );
    await super.inputText(
      addressData.PostCode,
      inputs.postCode.selector(this.claimantDefendantParty),
    );
  }

  async findAddress(postcode: string, index: number) {
    await super.inputText(postcode, inputs.postCodeLookup.selector(this.claimantDefendantParty));
    await super.clickButtonByName(buttons.findaddress.title);
    await super.expectSelector(dropdowns.addressList.selector(this.claimantDefendantParty));
    await super.selectFromDropdown(
      index,
      dropdowns.addressList.selector(this.claimantDefendantParty),
    );
  }

  async submit() {
    throw new Error('Method not implemented.');
  }
}
