import { Page } from '@playwright/test';
import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import StringHelper from '../../../../../../../helpers/string-helper.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import { Party } from '../../../../../../../models/partys.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import SolicitorReferenceFragment from '../../../../../fragments/solicitor-reference/solicitor-reference-fragment.ts';
import { subheadings } from './solicitor-references-defendant-response-content.ts';

@AllMethodsStep()
export default class SolicitorReferencesDefendantResponsePage extends ExuiPage(BasePage) {
  private defendantParty: Party;
  private solicitorParty: Party;
  private defendantSolicitorReferenceFragment: SolicitorReferenceFragment;

  constructor(
    page: Page,
    defendantSolicitorReferenceFragment: SolicitorReferenceFragment,
    defendantParty: Party,
    solicitorParty: Party,
  ) {
    super(page);
    this.defendantSolicitorReferenceFragment = defendantSolicitorReferenceFragment;
    this.defendantParty = defendantParty;
    this.solicitorParty = solicitorParty;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([super.verifyHeadings(ccdCaseData)], {
      axePageInsertName: StringHelper.capitalise(this.solicitorParty.key),
    });
  }

  async enterReference() {
    await this.defendantSolicitorReferenceFragment.enterReference();
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
