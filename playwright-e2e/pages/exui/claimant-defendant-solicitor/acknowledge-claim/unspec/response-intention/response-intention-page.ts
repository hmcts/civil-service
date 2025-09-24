import BasePage from '../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import ExuiPage from '../../../../exui-page/exui-page.ts';
import { radioButtons } from './response-intention-content.ts';
import CCDCaseData from '../../../../../../models/ccd/ccd-case-data.ts';
import { Party } from '../../../../../../models/partys.ts';
import { Page } from '@playwright/test';
import partys from '../../../../../../constants/partys.ts';
import StringHelper from '../../../../../../helpers/string-helper.ts';

@AllMethodsStep()
export default class ResponseIntentionPage extends ExuiPage(BasePage) {
  private defendantParty: Party;

  constructor(page: Page, defendantParty: Party) {
    super(page);
    this.defendantParty = defendantParty;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    super.runVerifications(
      [
        super.verifyHeadings(ccdCaseData),
        super.expectLabel(radioButtons.fullDefence.label, { count: 1 }),
        super.expectLabel(radioButtons.partAdmit.label, { count: 1 }),
        super.expectLabel(radioButtons.contestJurisdiction.label, { count: 1 }),
      ],
      { axePageInsertName: StringHelper.capitalise(this.defendantParty.key) },
    );
  }

  async selectRejectAll() {
    await super.clickBySelector(
      radioButtons.fullDefence.selector(this.defendantParty, partys.CLAIMANT_1),
    );
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
