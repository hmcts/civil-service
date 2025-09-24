import { Page } from '@playwright/test';
import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import { Party } from '../../../../../../../models/partys.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import {
  subheadings,
  radioButtons,
  inputs,
} from './disclosure-of-non-electronic-documents-content.ts';
import StringHelper from '../../../../../../../helpers/string-helper.ts';

@AllMethodsStep()
export default class DisclosureOfNonElectronicDocumentsPage extends ExuiPage(BasePage) {
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
        super.expectText(radioButtons.disclosureOfElectronicDocs.label, { count: 1 }),
        super.expectRadioYesLabel(
          radioButtons.disclosureOfElectronicDocs.yes.selector(this.claimantDefendantParty),
        ),
        super.expectRadioNoLabel(
          radioButtons.disclosureOfElectronicDocs.no.selector(this.claimantDefendantParty),
        ),
      ],
      { axePageInsertName: StringHelper.capitalise(this.solicitorParty.key) },
    );
  }

  async enterDetails() {
    await super.clickBySelector(
      radioButtons.disclosureOfElectronicDocs.yes.selector(this.claimantDefendantParty),
    );
    await super.expectText(radioButtons.standardDisclosure.label, { count: 1 });
    await super.clickBySelector(
      radioButtons.standardDisclosure.no.selector(this.claimantDefendantParty),
    );
    await super.expectLabel(inputs.bespokeDirections.label, { count: 1 });
    await super.inputText(
      `No directions required - ${this.claimantDefendantParty.key}`,
      inputs.bespokeDirections.selector(this.claimantDefendantParty),
    );
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
