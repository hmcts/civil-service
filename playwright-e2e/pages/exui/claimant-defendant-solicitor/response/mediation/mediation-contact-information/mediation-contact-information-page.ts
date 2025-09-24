import { Page } from '@playwright/test';
import { Party } from '../../../../../../models/partys.ts';
import BasePage from '../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../../exui-page/exui-page.ts';
import { subheadings, paragraphs, inputs } from './mediation-contact-information-content.ts';
import CaseDataHelper from '../../../../../../helpers/case-data-helper.ts';
import StringHelper from '../../../../../../helpers/string-helper.ts';

@AllMethodsStep()
export default class MediationContactInformationPage extends ExuiPage(BasePage) {
  private claimantDefendantParty: Party;
  private solicitorParty: Party;
  private mediationParty: Party;

  constructor(
    page: Page,
    claimantDefendantParty: Party,
    solicitorParty: Party,
    mediationParty: Party,
  ) {
    super(page);
    this.claimantDefendantParty = claimantDefendantParty;
    this.solicitorParty = solicitorParty;
    this.mediationParty = mediationParty;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications(
      [
        super.verifyHeadings(ccdCaseData),
        super.expectSubheading(subheadings.mediationContact, { count: 2 }),
        super.expectText(paragraphs.description1, { count: 1 }),
        super.expectText(paragraphs.description2, { count: 1 }),
        super.expectLabel(inputs.mediationFriendDetails.firstName.label, { count: 1 }),
        super.expectLabel(inputs.mediationFriendDetails.lastName.label, { count: 1 }),
        super.expectLabel(inputs.mediationFriendDetails.emailAddress.label, { count: 1 }),
        super.expectLabel(inputs.mediationFriendDetails.telephoneNumber.label, { count: 1 }),
      ],
      { axePageInsertName: StringHelper.capitalise(this.solicitorParty.key) },
    );
  }

  async enterMediationContactDetails() {
    const mediationData = CaseDataHelper.buildMediationData(this.mediationParty);

    await super.inputText(
      mediationData.firstName,
      inputs.mediationFriendDetails.firstName.selector(this.claimantDefendantParty),
    );

    await super.inputText(
      mediationData.lastName,
      inputs.mediationFriendDetails.lastName.selector(this.claimantDefendantParty),
    );

    await super.inputText(
      mediationData.emailAddress,
      inputs.mediationFriendDetails.emailAddress.selector(this.claimantDefendantParty),
    );

    await super.inputText(
      mediationData.phoneNumber,
      inputs.mediationFriendDetails.telephoneNumber.selector(this.claimantDefendantParty),
    );
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
