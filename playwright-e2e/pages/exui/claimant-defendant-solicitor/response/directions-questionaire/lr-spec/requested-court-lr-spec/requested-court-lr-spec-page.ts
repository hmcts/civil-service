import { Page } from '@playwright/test';
import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import { dropdowns, inputs, subheadings } from './requested-court-lr-spec-content.ts';
import { Party } from '../../../../../../../models/partys.ts';
import StringHelper from '../../../../../../../helpers/string-helper.ts';
import RemoteHearingSpecFragment from '../../../../../fragments/remote-hearing-spec/remote-hearing-spec-fragment.ts';
import preferredCourts from '../../../../../../../config/preferred-courts.ts';

@AllMethodsStep()
export default class RequestedCourtLRSpecPage extends ExuiPage(BasePage) {
  private remoteHearingSpecFragment: RemoteHearingSpecFragment;
  private defendantParty: Party;
  private solicitorParty: Party;

  constructor(
    page: Page,
    remoteHearingSpecFragment: RemoteHearingSpecFragment,
    defendantParty: Party,
    solicitorParty: Party,
  ) {
    super(page);
    this.remoteHearingSpecFragment = remoteHearingSpecFragment;
    this.defendantParty = defendantParty;
    this.solicitorParty = solicitorParty;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications(
      [
        super.verifyHeadings(ccdCaseData),
        super.expectSubheading(subheadings.courtLocation, { count: 1 }),
        super.expectLabel(inputs.preferredCourtReason.label, { count: 1 }),
        this.remoteHearingSpecFragment.verifyContent(),
      ],
      { axePageInsertName: StringHelper.capitalise(this.solicitorParty.key) },
    );
  }

  async selectCourtLocation() {
    await super.selectFromDropdown(
      preferredCourts[this.defendantParty.key].default,
      dropdowns.courtLocationDropdown.selector(this.defendantParty),
    );
    await super.inputText(
      `Court location reason - ${this.defendantParty.key}`,
      inputs.preferredCourtReason.selector(this.defendantParty),
    );
  }

  async selectYesRemoteHearing() {
    await this.remoteHearingSpecFragment.selectYes();
  }

  async selectNoRemoteHearing() {
    await this.remoteHearingSpecFragment.selectNo();
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
