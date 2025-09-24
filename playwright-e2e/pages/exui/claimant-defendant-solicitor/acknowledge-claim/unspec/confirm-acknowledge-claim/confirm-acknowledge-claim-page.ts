import BasePage from '../../../../../../base/base-page.ts';
import ExuiPage from '../../../../exui-page/exui-page.ts';
import CCDCaseData from '../../../../../../models/ccd/ccd-case-data.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import { confirmationHeading } from './confirm-acknowledge-claim-content.ts';
import DateHelper from '../../../../../../helpers/date-helper.ts';
import { Page } from '@playwright/test';
import { Party } from '../../../../../../models/partys.ts';
import partys from '../../../../../../constants/partys.ts';

@AllMethodsStep()
export default class ConfirmAcknowledgeClaimPage extends ExuiPage(BasePage) {
  private defendantParty: Party;

  constructor(page: Page, defendantParty: Party) {
    super(page);
    this.defendantParty = defendantParty;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    const responseDeadline = DateHelper.addToDate(
      this.defendantParty === partys.DEFENDANT_1
        ? ccdCaseData.respondent1ResponseDeadline
        : ccdCaseData.respondent2ResponseDeadline,
      {
        days: 14,
        workingDay: true,
      },
    );
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectHeading(confirmationHeading),
      super.expectSubheading(ccdCaseData.legacyCaseReference, { exact: false }),
      super.expectText(DateHelper.formatDateToString(responseDeadline), { exact: false }),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
