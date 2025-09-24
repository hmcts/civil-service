import { Page } from '@playwright/test';
import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import { heading, inputs } from './how-to-add-timeline-upload-content.ts';
import filePaths from '../../../../../../../config/file-paths.ts';
import { Party } from '../../../../../../../models/partys.ts';
import StringHelper from '../../../../../../../helpers/string-helper.ts';
import { getFormattedCaseId } from '../../../../../exui-page/exui-content.ts';

@AllMethodsStep()
export default class HowToAddTimelineUploadPage extends ExuiPage(BasePage) {
  private defendantParty: Party;
  private solicitorParty: Party;

  constructor(page: Page, defendantParty: Party, solicitorParty: Party) {
    super(page);
    this.defendantParty = defendantParty;
    this.solicitorParty = solicitorParty;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications(
      [
        super.expectHeading(heading, { exact: false }),
        super.expectHeading(getFormattedCaseId(ccdCaseData.id), { exact: false }),
        super.expectHeading(ccdCaseData.caseNamePublic, { exact: false }),
        super.expectText(inputs.upload.label, { count: 1 }),
      ],
      { axePageInsertName: StringHelper.capitalise(this.solicitorParty.key) },
    );
  }

  async uploadDoc() {
    await super.retryUploadFile(filePaths.testPdfFile, inputs.upload.selector(this.defendantParty));
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
