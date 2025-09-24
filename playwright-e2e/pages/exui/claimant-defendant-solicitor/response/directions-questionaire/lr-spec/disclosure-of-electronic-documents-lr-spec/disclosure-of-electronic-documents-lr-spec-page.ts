import { Page } from '@playwright/test';
import { Party } from '../../../../../../../models/partys.ts';
import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import {
  subheadings,
  inputs,
  radioButtons,
} from './disclosure-of-electronic-documents-lr-spec-content.ts';
import StringHelper from '../../../../../../../helpers/string-helper.ts';

@AllMethodsStep()
export default class DisclosureOfElectronicDocumentsLRSpecPage extends ExuiPage(BasePage) {
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
        super.expectSubheading(subheadings.disclosureOfDocs, { count: 1 }),
        super.expectLegend(radioButtons.disclosureOfElectronicDocs.label, { count: 1 }),
      ],
      { axePageInsertName: StringHelper.capitalise(this.solicitorParty.key) },
    );
  }

  async enterDetails() {
    await super.clickBySelector(
      radioButtons.disclosureOfElectronicDocs.no.selector(this.claimantDefendantParty),
    );
    // await super.expectText(radioButtons.agreement.label);
    await super.clickBySelector(radioButtons.agreement.no.selector(this.claimantDefendantParty));
    // await super.expectText(inputs.disagreementReason.label);
    await super.inputText(
      `Disagreement reason - ${this.claimantDefendantParty.key}`,
      inputs.disagreementReason.selector(this.claimantDefendantParty),
    );
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
