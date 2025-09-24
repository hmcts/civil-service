import BasePage from '../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import { heading, inputs, subheadings } from './solicitor-references-acknowledge-claim-content.ts';
import ExuiPage from '../../../../exui-page/exui-page.ts';
import SolicitorReferenceFragment from '../../../../fragments/solicitor-reference/solicitor-reference-fragment.ts';
import { Page } from '@playwright/test';
import { Party } from '../../../../../../models/partys.ts';
import StringHelper from '../../../../../../helpers/string-helper.ts';
import CCDCaseData from '../../../../../../models/ccd/ccd-case-data.ts';
import partys from '../../../../../../constants/partys.ts';
import { getFormattedCaseId } from '../../../../exui-page/exui-content.ts';

@AllMethodsStep()
export default class SolicitorReferencesAcknowledgeClaimPage extends ExuiPage(BasePage) {
  private defendantParty: Party;
  private defendantSolicitorReferenceFragment: SolicitorReferenceFragment;

  constructor(
    page: Page,
    defendantSolicitorReferenceFragment: SolicitorReferenceFragment,
    defendantParty: Party,
  ) {
    super(page);
    this.defendantParty = defendantParty;
    this.defendantSolicitorReferenceFragment = defendantSolicitorReferenceFragment;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    if (this.defendantParty === partys.DEFENDANT_1)
      await super.runVerifications(
        [
          super.expectSubheading(subheadings.yourFileReference),
          this.defendantSolicitorReferenceFragment.verifyContent(),
        ],
        { axePageInsertName: StringHelper.capitalise(this.defendantParty.key) },
      );
    else
      await super.runVerifications(
        [
          super.expectHeading(heading),
          super.expectHeading(getFormattedCaseId(ccdCaseData.id), { exact: false }),
          super.expectHeading(ccdCaseData.caseNamePublic, { exact: false }),
          super.expectLabel(inputs.fileRefDS2.label),
        ],
        { axePageInsertName: StringHelper.capitalise(this.defendantParty.key) },
      );
  }

  async enterReference() {
    await this.defendantSolicitorReferenceFragment.enterReference();
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
