import { Page } from '@playwright/test';
import { Party } from '../../../../../../../models/partys.ts';
import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import { subheadings, radioButtons, inputs } from './fixed-recoverable-costs-content.ts';
import StringHelper from '../../../../../../../helpers/string-helper.ts';

@AllMethodsStep()
export default class FixedRecoverableCostsPage extends ExuiPage(BasePage) {
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
        super.expectSubheading(subheadings.fixedRecoverableCosts, { count: 1 }),
        super.expectLegend(radioButtons.fixedRecoverableCosts.label, {
          count: 1,
        }),
        super.expectRadioYesLabel(
          radioButtons.fixedRecoverableCosts.yes.selector(this.claimantDefendantParty),
        ),
        super.expectRadioNoLabel(
          radioButtons.fixedRecoverableCosts.no.selector(this.claimantDefendantParty),
        ),
      ],
      { axePageInsertName: StringHelper.capitalise(this.solicitorParty.key) },
    );
  }

  async selectNo() {
    await super.clickBySelector(
      radioButtons.fixedRecoverableCosts.no.selector(this.claimantDefendantParty),
    );
    await super.expectLabel(inputs.fixedRecoverableCostsReason.label, { count: 1 });
    await super.inputText(
      `No explanation - ${this.claimantDefendantParty.key}`,
      inputs.fixedRecoverableCostsReason.selector(this.claimantDefendantParty),
    );
  }

  async selectYes() {
    await super.clickBySelector(
      radioButtons.fixedRecoverableCosts.yes.selector(this.claimantDefendantParty),
    );
    await super.clickBySelector(
      radioButtons.complexityBands.band1.selector(this.claimantDefendantParty),
    );
    await super.expectText(radioButtons.complexityBandAgreed.label, {
      count: 1,
    });
    await super.clickBySelector(
      radioButtons.complexityBandAgreed.yes.selector(this.claimantDefendantParty),
    );
    await super.expectLabel(inputs.fixedRecoverableCostsReason.label, { count: 1 });
    await super.inputText(
      `No explanation - ${this.claimantDefendantParty.key}`,
      inputs.fixedRecoverableCostsReason.selector(this.claimantDefendantParty),
    );
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
