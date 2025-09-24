import { Page } from '@playwright/test';
import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import { inputs, radioButtons, subheadings } from './small-claim-experts-content.ts';
import { Party } from '../../../../../../../models/partys.ts';
import CaseDataHelper from '../../../../../../../helpers/case-data-helper.ts';

@AllMethodsStep()
export default class SmallClaimExpertsPage extends ExuiPage(BasePage) {
  private claimantDefendantParty: Party;
  private solicitorParty: Party;
  private expertParty: Party;

  constructor(
    page: Page,
    claimantDefendantParty: Party,
    solicitorParty: Party,
    expertParty: Party,
  ) {
    super(page);
    this.claimantDefendantParty = claimantDefendantParty;
    this.solicitorParty = solicitorParty;
    this.expertParty = expertParty;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications(
      [
        super.verifyHeadings(ccdCaseData),
        super.expectSubheading(subheadings.experts, { count: 1 }),
        super.expectLegend(radioButtons.expertsRequired.label, { count: 1 }),
      ],
      { axePageInsertName: this.solicitorParty.key },
    );
  }

  async selectYesExperts() {
    await super.clickBySelector(
      radioButtons.expertsRequired.yes.selector(this.claimantDefendantParty),
    );
  }

  async useNoExperts() {
    await super.clickBySelector(
      radioButtons.expertsRequired.no.selector(this.claimantDefendantParty),
    );
  }

  async enterExpertDetails() {
    const expertData = CaseDataHelper.buildExpertData(this.expertParty);
    await super.inputText(
      expertData.firstName,
      inputs.expert.firstName.selector(this.claimantDefendantParty),
    );
    await super.inputText(
      expertData.lastName,
      inputs.expert.lastName.selector(this.claimantDefendantParty),
    );
    await super.inputText(
      expertData.phoneNumber,
      inputs.expert.phoneNumber.selector(this.claimantDefendantParty),
    );
    await super.inputText(
      expertData.emailAddress,
      inputs.expert.email.selector(this.claimantDefendantParty),
    );
    await super.inputText(
      expertData.fieldOfExpertise,
      inputs.expert.expertise.selector(this.claimantDefendantParty),
    );
    await super.inputText(
      expertData.whyRequired,
      inputs.expert.whyRequired.selector(this.claimantDefendantParty),
    );
    await super.inputText(
      expertData.estimatedCost,
      inputs.expert.estimatedCost.selector(this.claimantDefendantParty),
    );
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
