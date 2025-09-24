import { Page } from '@playwright/test';
import { Party } from '../../../../../../../models/partys.ts';
import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import { subheadings, inputs, radioButtons, buttons } from './experts-content.ts';
import StringHelper from '../../../../../../../helpers/string-helper.ts';
import CaseDataHelper from '../../../../../../../helpers/case-data-helper.ts';

@AllMethodsStep()
export default class ExpertPage extends ExuiPage(BasePage) {
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
        super.expectRadioYesLabel(
          radioButtons.expertsRequired.yes.selector(this.claimantDefendantParty),
        ),
        super.expectRadioNoLabel(
          radioButtons.expertsRequired.no.selector(this.claimantDefendantParty),
        ),
      ],
      { axePageInsertName: StringHelper.capitalise(this.solicitorParty.key) },
    );
  }

  async useExperts() {
    await super.clickBySelector(
      radioButtons.expertsRequired.yes.selector(this.claimantDefendantParty),
    );
    await super.clickBySelector(
      radioButtons.expertReports.notObtained.selector(this.claimantDefendantParty),
    );
    await super.clickBySelector(radioButtons.jointExpert.no.selector(this.claimantDefendantParty));
  }

  async addNewExpert() {
    await super.clickBySelector(buttons.addNew.selector(this.claimantDefendantParty));
  }

  async enterExpertDetails() {
    const expertData = CaseDataHelper.buildExpertData(this.expertParty);
    await super.inputText(
      expertData.firstName,
      inputs.expertDetails.firstName.selector(this.claimantDefendantParty, this.expertParty),
    );

    await super.inputText(
      expertData.lastName,
      inputs.expertDetails.lastName.selector(this.claimantDefendantParty, this.expertParty),
    );

    await super.inputText(
      expertData.emailAddress,
      inputs.expertDetails.emailAddress.selector(this.claimantDefendantParty, this.expertParty),
    );

    await super.inputText(
      expertData.phoneNumber,
      inputs.expertDetails.phoneNumber.selector(this.claimantDefendantParty, this.expertParty),
    );

    await super.inputText(
      expertData.fieldOfExpertise,
      inputs.expertDetails.expertise.selector(this.claimantDefendantParty, this.expertParty),
    );

    await super.inputText(
      expertData.whyRequired,
      inputs.expertDetails.whyRequired.selector(this.claimantDefendantParty, this.expertParty),
    );

    await super.inputText(
      expertData.estimatedCost,
      inputs.expertDetails.estimatedCost.selector(this.claimantDefendantParty, this.expertParty),
    );
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
