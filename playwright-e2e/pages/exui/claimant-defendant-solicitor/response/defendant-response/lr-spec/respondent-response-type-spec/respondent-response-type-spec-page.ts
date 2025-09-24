import { Page } from '@playwright/test';
import BasePage from '../../../../../../../base/base-page.ts';
import partys from '../../../../../../../constants/partys.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import { Party } from '../../../../../../../models/partys.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import { radioButtons } from './respondent-response-type-spec-content.ts';
import StringHelper from '../../../../../../../helpers/string-helper.ts';

@AllMethodsStep()
export default class RespondentResponseTypeSpecPage extends ExuiPage(BasePage) {
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
        super.verifyHeadings(ccdCaseData),
        super.expectLabel(radioButtons.fullDefence.label, { count: 1 }),
        super.expectLabel(radioButtons.fullAdmit.label, { count: 1 }),
        super.expectLabel(radioButtons.partAdmit.label, { count: 1 }),
        super.expectLabel(radioButtons.counterClaim.label, { count: 1 }),
      ],
      { axePageInsertName: StringHelper.capitalise(this.solicitorParty.key) },
    );
  }

  async selectFullDefence() {
    await super.clickBySelector(radioButtons.fullDefence.selector(this.defendantParty));
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
