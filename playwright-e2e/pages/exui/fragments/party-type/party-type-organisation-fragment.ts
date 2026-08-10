import { Page } from '@playwright/test';
import BasePage from '../../../../base/base-page';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import { Party } from '../../../../models/users/partys';
import ExuiPage from '../../mixin-pages/exui-page/exui-page';
import { inputs } from './party-type-content';
import claimantDefendantPartyTypes from '../../../../constants/users/claimant-defendant-party-types';
import CaseDataHelper from '../../../../helpers/case-data-helper';

@AllMethodsStep()
export default class PartyTypeOrganisationFragment extends ExuiPage(BasePage) {
  private partyType = claimantDefendantPartyTypes.ORGANISATION;
  private claimantDefendantParty: Party;

  constructor(page: Page, claimantDefendantParty: Party) {
    super(page);
    this.claimantDefendantParty = claimantDefendantParty;
  }

  async verifyContent() {
    await super.runVerifications(
      [super.expectLabel(inputs.name.label, { count: 1 }), super.expectLabel(inputs.email.label)],
      {
        runAxe: false,
      },
    );
  }

  async enterOrganisationDetails(update?: boolean) {
    const organisationData = CaseDataHelper.buildClaimantAndDefendantData(
      this.claimantDefendantParty,
      this.partyType,
      update,
    );
    await super.inputText(
      organisationData.organisationName,
      inputs.name.selector(this.claimantDefendantParty, this.partyType),
    );
    await super.inputText(
      organisationData.partyEmail,
      inputs.email.selector(this.claimantDefendantParty),
    );
  }

  async submit() {
    throw new Error('Method not implemented.');
  }
}
