import { Page } from '@playwright/test';
import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import { subheadings, buttons, inputs, radioButtons } from './witnesses-content.ts';
import { Party } from '../../../../../../../models/partys.ts';
import StringHelper from '../../../../../../../helpers/string-helper.ts';
import CaseDataHelper from '../../../../../../../helpers/case-data-helper.ts';

@AllMethodsStep()
export default class WitnessesPage extends ExuiPage(BasePage) {
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
        super.expectLegend(radioButtons.witnessesRequired.label, { count: 1 }),
        super.expectRadioYesLabel(
          radioButtons.witnessesRequired.yes.selector(this.claimantDefendantParty),
        ),
        super.expectRadioNoLabel(
          radioButtons.witnessesRequired.no.selector(this.claimantDefendantParty),
        ),
      ],
      { axePageInsertName: StringHelper.capitalise(this.claimantDefendantParty.key) },
    );
  }

  async selectYesWitnesses() {
    await super.clickBySelector(
      radioButtons.witnessesRequired.yes.selector(this.claimantDefendantParty),
    );
  }

  async selectNoWitnesses() {
    await super.clickBySelector(
      radioButtons.witnessesRequired.no.selector(this.claimantDefendantParty),
    );
  }

  async addWitness() {
    await super.clickBySelector(buttons.addNewWitness.selector(this.claimantDefendantParty));
  }

  async enterWitnessDetails() {
    const witnessData = CaseDataHelper.buildWitnessData(this.witnessParty);
    await super.inputText(
      witnessData.firstName,
      inputs.witnessDetails.firstName.selector(this.claimantDefendantParty, this.witnessParty),
    );
    await super.inputText(
      witnessData.lastName,
      inputs.witnessDetails.lastName.selector(this.claimantDefendantParty, this.witnessParty),
    );
    await super.inputText(
      witnessData.phoneNumber,
      inputs.witnessDetails.number.selector(this.claimantDefendantParty, this.witnessParty),
    );
    await super.inputText(
      witnessData.emailAddress,
      inputs.witnessDetails.email.selector(this.claimantDefendantParty, this.witnessParty),
    );
    await super.inputText(
      witnessData.reasonForWitness,
      inputs.witnessDetails.whatEvent.selector(this.claimantDefendantParty, this.witnessParty),
    );
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
