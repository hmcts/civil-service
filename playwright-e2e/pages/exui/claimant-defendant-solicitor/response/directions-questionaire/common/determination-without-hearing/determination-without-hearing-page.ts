import { Page } from '@playwright/test';
import { Party } from '../../../../../../../models/partys.ts';
import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import StringHelper from '../../../../../../../helpers/string-helper.ts';
import { heading, inputs, radioButtons } from './determination-without-hearing-content.ts';
import YesOrNoFragment from '../../../../../fragments/yes-or-no/yes-or-no-fragment.ts';
import { getFormattedCaseId } from '../../../../../exui-page/exui-content.ts';

@AllMethodsStep()
export default class DeterminationWithoutHearingPage extends ExuiPage(BasePage) {
  private yesOrNoFragment: YesOrNoFragment;
  private claimantDefendantParty: Party;
  private solicitorParty: Party;

  constructor(
    page: Page,
    yesOrNoFragment: YesOrNoFragment,
    claimantDefendantParty: Party,
    solicitorParty: Party,
  ) {
    super(page);
    this.yesOrNoFragment = yesOrNoFragment;
    this.claimantDefendantParty = claimantDefendantParty;
    this.solicitorParty = solicitorParty;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications(
      [
        super.expectHeading(heading),
        super.expectHeading(getFormattedCaseId(ccdCaseData.id), { exact: false }),
        super.expectHeading(ccdCaseData.caseNamePublic, { exact: false }),
        super.expectHeading(heading),
        super.expectLegend(radioButtons.deterWithoutHearing.label, { count: 1 }),
        this.yesOrNoFragment.verifyContent(
          radioButtons.deterWithoutHearing.selectorKey(this.claimantDefendantParty),
        ),
      ],
      {
        axePageInsertName: StringHelper.capitalise(this.solicitorParty.key),
      },
    );
  }

  async selectYes() {
    await this.yesOrNoFragment.selectYes(
      radioButtons.deterWithoutHearing.selectorKey(this.claimantDefendantParty),
    );
  }

  async selectNo() {
    await this.yesOrNoFragment.selectNo(
      radioButtons.deterWithoutHearing.selectorKey(this.claimantDefendantParty),
    );
    await super.expectLabel(inputs.deterWithoutHearing.why.label, { count: 1 });
    await super.inputText(
      `Determination with hearing is not needed - ${this.solicitorParty.key}`,
      inputs.deterWithoutHearing.why.selector(this.claimantDefendantParty),
    );
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
