import { Page } from '@playwright/test';
import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import { subheadings, inputs, radioButtons } from './disclosure-report-content.ts';
import { Party } from '../../../../../../../models/partys.ts';
import StringHelper from '../../../../../../../helpers/string-helper.ts';

@AllMethodsStep()
export default class DisclosureReportPage extends ExuiPage(BasePage) {
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
        super.expectSubheading(subheadings.report, { count: 1 }),
        super.expectText(radioButtons.disclosureReportFilledAndServed.label, { count: 1 }),
        super.expectText(radioButtons.disclosureProposalAgreed.label, { count: 1 }),
      ],
      { axePageInsertName: StringHelper.capitalise(this.solicitorParty.key) },
    );
  }

  async enterDetails() {
    await super.clickBySelector(
      radioButtons.disclosureReportFilledAndServed.no.selector(this.claimantDefendantParty),
    );
    await super.clickBySelector(
      radioButtons.disclosureProposalAgreed.yes.selector(this.claimantDefendantParty),
    );
    await super.expectLabel(inputs.draftOrderNumber.label, { ignoreDuplicates: true });
    await super.inputText('12345', inputs.draftOrderNumber.selector(this.claimantDefendantParty));
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
