import { Page } from '@playwright/test';
import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import {
  subheadings,
  inputs,
  checkboxes,
  radioButtons,
} from './file-directions-questionnaire-content.ts';
import { Party } from '../../../../../../../models/partys.ts';
import StringHelper from '../../../../../../../helpers/string-helper.ts';

@AllMethodsStep()
export default class FileDirectionsQuestionnairePage extends ExuiPage(BasePage) {
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
        super.expectSubheading(subheadings.fileDQ, { count: 1 }),
        super.expectLabel(checkboxes.fileDQConfirm.label, { count: 1 }),
        super.expectRadioYesLabel(
          radioButtons.oneMonthStay.yes.selector(this.claimantDefendantParty),
        ),
        super.expectRadioNoLabel(
          radioButtons.oneMonthStay.no.selector(this.claimantDefendantParty),
        ),
        super.expectRadioYesLabel(
          radioButtons.protocolComplied.yes.selector(this.claimantDefendantParty),
        ),
        super.expectRadioNoLabel(
          radioButtons.protocolComplied.no.selector(this.claimantDefendantParty),
        ),
      ],
      { axePageInsertName: StringHelper.capitalise(this.solicitorParty.key) },
    );
  }

  async enterDetails() {
    await super.clickBySelector(checkboxes.fileDQConfirm.selector(this.claimantDefendantParty));
    await super.clickBySelector(radioButtons.oneMonthStay.no.selector(this.claimantDefendantParty));
    await super.clickBySelector(
      radioButtons.protocolComplied.no.selector(this.claimantDefendantParty),
    );
    await super.inputText(
      `No explanation - ${this.claimantDefendantParty.key}`,
      inputs.noProtocolCompliedReason.selector(this.claimantDefendantParty),
    );
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
