import { Page } from '@playwright/test';
import BasePage from '../../../../base/base-page';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import { Party } from '../../../../models/partys';
import ExuiPage from '../../exui-page/exui-page';
import { radioButtons, buttons, inputs, subheadings, links } from './litigation-friend-content';
import filePaths from '../../../../config/file-paths';
import CaseDataHelper from '../../../../helpers/case-data-helper';
import partys from '../../../../constants/partys';

@AllMethodsStep()
export default class LitigationFriendFragment extends ExuiPage(BasePage) {
  private litigationFriendParty: Party;

  constructor(page: Page, litigationFriendParty: Party) {
    super(page);
    this.litigationFriendParty = litigationFriendParty;
  }

  async verifyContent() {
    await super.runVerifications(
      [
        super.expectLabel(inputs.litigationFriendDetails.firstName.label),
        super.expectLabel(inputs.litigationFriendDetails.lastName.label),
        super.expectLabel(inputs.litigationFriendDetails.email.label),
        super.expectLabel(inputs.litigationFriendDetails.phoneNumber.label),
        super.expectSubheading(subheadings.uploadcertificate),
      ],
      {
        runAxe: false,
      },
    );
  }

  async enterLitigationFriendDetails() {
    const claimantLitigationFriendData = CaseDataHelper.buildLitigationFriendData(
      this.litigationFriendParty,
    );
    await super.inputText(
      claimantLitigationFriendData.firstName,
      inputs.litigationFriendDetails.firstName.selector(this.litigationFriendParty),
    );
    await super.inputText(
      claimantLitigationFriendData.lastName,
      inputs.litigationFriendDetails.lastName.selector(this.litigationFriendParty),
    );
    await super.inputText(
      claimantLitigationFriendData.emailAddress,
      inputs.litigationFriendDetails.email.selector(this.litigationFriendParty),
    );
    await super.inputText(
      claimantLitigationFriendData.phoneNumber,
      inputs.litigationFriendDetails.phoneNumber.selector(this.litigationFriendParty),
    );
  }

  async chooseYesSameAddress() {
    await super.clickBySelector(radioButtons.address.yes.selector(this.litigationFriendParty));
  }

  async chooseNoSameAddress() {
    const addressData = CaseDataHelper.buildAddressData(this.litigationFriendParty);
    await super.clickBySelector(radioButtons.address.no.selector(this.litigationFriendParty));
    if (this.litigationFriendParty !== partys.CLAIMANT_2_LITIGATION_FRIEND)
      await super.clickLink(links.cannotFindAddress.title);
    await super.inputText(
      addressData.AddressLine1,
      inputs.address.addressLine1.selector(this.litigationFriendParty),
    );
    await super.inputText(
      addressData.AddressLine2,
      inputs.address.addressLine2.selector(this.litigationFriendParty),
    );
    await super.inputText(
      addressData.AddressLine3,
      inputs.address.addressLine3.selector(this.litigationFriendParty),
    );
    await super.inputText(
      addressData.PostTown,
      inputs.address.postTown.selector(this.litigationFriendParty),
    );
    if (this.litigationFriendParty !== partys.CLAIMANT_2_LITIGATION_FRIEND)
      await super.inputText(
        addressData.County,
        inputs.address.county.selector(this.litigationFriendParty),
      );
    await super.inputText(
      addressData.Country,
      inputs.address.country.selector(this.litigationFriendParty),
    );
    await super.inputText(
      addressData.PostCode,
      inputs.address.postCode.selector(this.litigationFriendParty),
    );
  }

  async uploadCertificateOfSuitability() {
    await super.clickBySelector(buttons.addNewCertificate.selector(this.litigationFriendParty));
    await super.expectLabel(inputs.certificateOfSuitability.uploadDoc.label, {
      count: 1,
    });
    await super.retryUploadFile(
      filePaths.testPdfFile,
      inputs.certificateOfSuitability.uploadDoc.selector(this.litigationFriendParty),
    );
  }

  async submit() {
    throw new Error('Method not implemented.');
  }
}
