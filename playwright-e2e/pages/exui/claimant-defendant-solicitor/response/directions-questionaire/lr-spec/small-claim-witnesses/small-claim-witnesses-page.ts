import { Page } from '@playwright/test';
import { Party } from '../../../../../../../models/partys.ts';
import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import { buttons, inputs, radioButtons, subheadings } from './small-claim-witnesses-content.ts';
import CaseDataHelper from '../../../../../../../helpers/case-data-helper.ts';
import StringHelper from '../../../../../../../helpers/string-helper.ts';

@AllMethodsStep()
export default class SmallClaimWitnessesPage extends ExuiPage(BasePage) {
  private claimantDefendantParty: Party;
  private solicitorParty: Party;
  private witnessParty: Party;

  constructor(
    page: Page,
    claimantDefendantParty: Party,
    solicitorParty: Party,
    witnessParty: Party,
  ) {
    super(page);
    this.claimantDefendantParty = claimantDefendantParty;
    this.solicitorParty = solicitorParty;
    this.witnessParty = witnessParty;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications(
      [
        super.verifyHeadings(ccdCaseData),
        super.expectSubheading(subheadings.witnesses, { count: 1 }),
        super.expectSubheading(subheadings.partyWitnesses(this.claimantDefendantParty), {
          count: 1,
        }),
        super.expectLegend(radioButtons.witnessesRequired.label, { count: 1 }),
      ],
      { axePageInsertName: StringHelper.capitalise(this.solicitorParty.key) },
    );
  }

  async selectYes() {
    await super.clickBySelector(
      radioButtons.witnessesRequired.yes.selector(this.claimantDefendantParty),
    );
  }

  async selectNo() {
    await super.clickBySelector(
      radioButtons.witnessesRequired.no.selector(this.claimantDefendantParty),
    );
  }

  async addWitness() {
    await super.clickBySelector(buttons.addNewWitness.selector(this.claimantDefendantParty));
  }

  async enterWitnessDetails() {
    const defendantWitnessData = CaseDataHelper.buildWitnessData(this.witnessParty);
    await super.inputText(
      defendantWitnessData.firstName,
      inputs.witnessDetails.firstName.selector(this.claimantDefendantParty, this.witnessParty),
    );
    await super.inputText(
      defendantWitnessData.lastName,
      inputs.witnessDetails.lastName.selector(this.claimantDefendantParty, this.witnessParty),
    );
    await super.inputText(
      defendantWitnessData.phoneNumber,
      inputs.witnessDetails.phoneNumber.selector(this.claimantDefendantParty, this.witnessParty),
    );
    await super.inputText(
      defendantWitnessData.emailAddress,
      inputs.witnessDetails.email.selector(this.claimantDefendantParty, this.witnessParty),
    );
    await super.inputText(
      defendantWitnessData.reasonForWitness,
      inputs.witnessDetails.whatEvent.selector(this.claimantDefendantParty, this.witnessParty),
    );
  }

  async enterWitnessNumber() {
    await super.inputText(1, inputs.witnessNumber.selector(this.claimantDefendantParty));
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
