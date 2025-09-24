import { Page } from '@playwright/test';
import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import { subheadings, radioButtons, inputs } from './hearing-support-content.ts';
import { Party } from '../../../../../../../models/partys.ts';
import StringHelper from '../../../../../../../helpers/string-helper.ts';

@AllMethodsStep()
export default class HearingSupportPage extends ExuiPage(BasePage) {
  private claimantDefendantParty: Party;
  private solicitorParty: Party;

  constructor(page: Page, claimantDefendantParty: Party, solicitorParty: Party) {
    super(page);
    this.claimantDefendantParty = claimantDefendantParty;
    this.solicitorParty = solicitorParty;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications(
      [
        super.verifyHeadings(ccdCaseData),
        super.expectSubheading(subheadings.supportNeeds, { count: 1 }),
        super.expectLegend(radioButtons.supportRequirements.label, { count: 1 }),
        super.expectRadioYesLabel(
          radioButtons.supportRequirements.yes.selector(this.claimantDefendantParty),
        ),
        super.expectRadioNoLabel(
          radioButtons.supportRequirements.no.selector(this.claimantDefendantParty),
        ),
      ],
      { axePageInsertName: StringHelper.capitalise(this.solicitorParty.key) },
    );
  }

  async selectYes() {
    await super.clickBySelector(
      radioButtons.supportRequirements.yes.selector(this.claimantDefendantParty),
    );
  }

  async enterSupportRequirementsAdditional() {
    await super.inputText(
      `Support requirements for ${this.claimantDefendantParty.key}`,
      inputs.supportRequirementsAdditional.selector(this.claimantDefendantParty),
    );
  }

  async selectNo() {
    await super.clickBySelector(
      radioButtons.supportRequirements.no.selector(this.claimantDefendantParty),
    );
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
