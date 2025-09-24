import { Page } from '@playwright/test';
import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import { radioButtons, inputs, subheadings } from './application-content.ts';
import { Party } from '../../../../../../../models/partys.ts';
import StringHelper from '../../../../../../../helpers/string-helper.ts';

@AllMethodsStep()
export default class ApplicationPage extends ExuiPage(BasePage) {
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
        super.expectSubheading(subheadings.application),
        super.expectLabel(inputs.otherInformation.label, { count: 1 }),
      ],
      { axePageInsertName: StringHelper.capitalise(this.solicitorParty.key) },
    );
  }

  async selectYes() {
    await super.clickBySelector(radioButtons.application.yes.selector(this.claimantDefendantParty));
    await super.inputText(
      `Reason - ${this.claimantDefendantParty.key}`,
      inputs.whatFor.selector(this.claimantDefendantParty),
    );
  }

  async selectNo() {
    await super.clickBySelector(radioButtons.application.no.selector(this.claimantDefendantParty));
  }

  async enterAdditionalInformation() {
    await super.inputText(
      `Additional information - ${this.claimantDefendantParty.key}`,
      inputs.otherInformation.selector(this.claimantDefendantParty),
    );
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
