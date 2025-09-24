import { Page } from '@playwright/test';
import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import { subheadings, paragraphs, radioButtons } from './language-content.ts';
import { Party } from '../../../../../../../models/partys.ts';
import StringHelper from '../../../../../../../helpers/string-helper.ts';

@AllMethodsStep()
export default class LanguagePage extends ExuiPage(BasePage) {
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
        super.expectSubheading(subheadings.welsh, { count: 1 }),
        super.expectText(paragraphs.descriptionText, { count: 1 }),
        super.expectLabel(radioButtons.courtLanguage.welsh.label, { count: 2 }),
        super.expectLabel(radioButtons.courtLanguage.english.label, { count: 2 }),
        super.expectLabel(radioButtons.courtLanguage.welshAndEnglish.label, { count: 2 }),
        super.expectLegend(radioButtons.courtLanguage.label, { count: 1 }),
        super.expectLegend(radioButtons.documentLanguage.label, { count: 1 }),
      ],
      { axePageInsertName: StringHelper.capitalise(this.solicitorParty.key) },
    );
  }

  async selectEnglishAndWelsh() {
    await super.clickBySelector(
      radioButtons.courtLanguage.welshAndEnglish.selector(this.claimantDefendantParty),
    );
    await super.clickBySelector(
      radioButtons.documentLanguage.welshAndEnglish.selector(this.claimantDefendantParty),
    );
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
