import { Page } from '@playwright/test';
import BasePage from '../../../../../../../base/base-page.ts';
import filePaths from '../../../../../../../config/file-paths.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import { inputs, subheadings } from './upload-defendant-response-spec-content.ts';
import { Party } from '../../../../../../../models/partys.ts';
import StringHelper from '../../../../../../../helpers/string-helper.ts';

@AllMethodsStep()
export default class UploadDefendantResponseSpecPage extends ExuiPage(BasePage) {
  private defendantParty: Party;
  private solicitorParty: Party;

  constructor(page: Page, defendantParty: Party, solicitorParty: Party) {
    super(page);
    this.defendantParty = defendantParty;
    this.solicitorParty = solicitorParty;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications(
      [super.verifyHeadings(ccdCaseData), super.expectSubheading(subheadings.uploadEvidence)],
      { axePageInsertName: StringHelper.capitalise(this.solicitorParty.key) },
    );
  }

  async enterDisputeReason() {
    await super.inputText(
      `This is Defendant ${this.defendantParty.key}'s reason`,
      inputs.disputeReason.selector(this.defendantParty),
    );
    await super.retryUploadFile(
      filePaths.testPdfFile,
      inputs.uploadEvidence.selector(this.defendantParty),
    );
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
