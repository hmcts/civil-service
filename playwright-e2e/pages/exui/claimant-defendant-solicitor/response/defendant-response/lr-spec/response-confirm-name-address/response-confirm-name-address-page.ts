import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import { radioButtons } from './response-confirm-name-address-content.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import { Page } from '@playwright/test';
import { Party } from '../../../../../../../models/partys.ts';
import StringHelper from '../../../../../../../helpers/string-helper.ts';

@AllMethodsStep()
export default class ResponseConfirmNameAddressPage extends ExuiPage(BasePage) {
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
        super.expectNoText('chgfgh', { exact: true, all: true }),
        super.expectLegend(radioButtons.address.label, { exact: true, count: 1 }),
        super.expectRadioYesLabel(
          radioButtons.address.yes.selector(this.defendantParty, this.solicitorParty),
        ),
        super.expectRadioNoLabel(
          radioButtons.address.no.selector(this.defendantParty, this.solicitorParty),
        ),
      ],
      { axePageInsertName: StringHelper.capitalise(this.solicitorParty.key) },
    );
  }

  async selectYesAddress() {
    await super.clickBySelector(
      radioButtons.address.yes.selector(this.defendantParty, this.solicitorParty),
    );
  }

  async selectNoAddress() {
    await super.clickBySelector(
      radioButtons.address.no.selector(this.defendantParty, this.solicitorParty),
    );
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
