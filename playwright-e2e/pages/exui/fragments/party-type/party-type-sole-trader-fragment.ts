import { Page } from '@playwright/test';
import BasePage from '../../../../base/base-page';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import { Party } from '../../../../models/users/partys';
import ExuiPage from '../../mixin-pages/exui-page/exui-page';
import { inputs } from './party-type-content';
import claimantDefendantPartyTypes from '../../../../constants/users/claimant-defendant-party-types';
import CaseDataHelper from '../../../../helpers/case-data-helper';
import PartyType from '../../../../constants/users/party-types';
import DateOfBirthFragment from '../date/date-of-birth-fragment';

@AllMethodsStep()
export default class PartyTypeSoleTraderFragment extends ExuiPage(BasePage) {
  private dateOfBirthFragment: DateOfBirthFragment;
  private claimantDefendantPartyType = claimantDefendantPartyTypes.SOLE_TRADER;
  private claimantDefendantParty: Party;

  constructor(page: Page, claimantDefendantParty: Party) {
    super(page);
    this.claimantDefendantParty = claimantDefendantParty;
    this.dateOfBirthFragment = new DateOfBirthFragment(page);
  }

  async verifyContent() {
    if (this.claimantDefendantParty.partyType === PartyType.CLAIMANT) {
      await super.expectLegend(inputs.dateOfBirth.label, { count: 1 });
      this.dateOfBirthFragment.verifyContent(this.claimantDefendantPartyType);
    }
    await super.runVerifications(
      [
        super.expectLabel(inputs.firstName.label, { count: 1 }),
        super.expectLabel(inputs.lastName.label, { count: 1 }),
        super.expectText(inputs.dateOfBirth.label, { count: 1 }),
        super.expectLabel(inputs.tradingAs.label),
        super.expectLabel(inputs.email.label),
        super.expectLabel(inputs.phone.label),
      ],
      {
        runAxe: false,
      },
    );
  }

  async enterSoleTraderDetails(update?: boolean) {
    const soleTraderData = CaseDataHelper.buildClaimantAndDefendantData(
      this.claimantDefendantParty,
      this.claimantDefendantPartyType,
      update
    );
    await super.inputText(
      soleTraderData.soleTraderTitle,
      inputs.title.selector(this.claimantDefendantParty, this.claimantDefendantPartyType),
    );
    await super.inputText(
      soleTraderData.soleTraderFirstName,
      inputs.firstName.selector(this.claimantDefendantParty, this.claimantDefendantPartyType),
    );
    await super.inputText(
      soleTraderData.soleTraderLastName,
      inputs.lastName.selector(this.claimantDefendantParty, this.claimantDefendantPartyType),
    );
    await super.inputText(
      soleTraderData.soleTraderTradingAs,
      inputs.tradingAs.selector(this.claimantDefendantParty, this.claimantDefendantPartyType),
    );
    if (this.claimantDefendantParty.partyType === PartyType.CLAIMANT) {
      await this.dateOfBirthFragment.enterDate(
        this.claimantDefendantParty,
        this.claimantDefendantPartyType,
      );
    }
    await super.inputText(
      soleTraderData.partyEmail,
      inputs.email.selector(this.claimantDefendantParty),
    );
    await super.inputText(
      soleTraderData.partyPhone,
      inputs.phone.selector(this.claimantDefendantParty),
    );
  }

  async submit() {
    throw new Error('Method not implemented.');
  }
}
