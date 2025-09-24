import BasePage from '../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import DateHelper from '../../../../../../helpers/date-helper.ts';
import ExuiPage from '../../../../exui-page/exui-page.ts';
import {
  subheadings,
  radioButtons,
  dropdowns,
  inputs,
} from './hearing-support-requirements-field-dj-content.ts';
import DateFragment from '../../../../fragments/date/date-fragment';
import partys from '../../../../../../constants/partys.ts';
import preferredCourts from '../../../../../../config/preferred-courts.ts';
import CaseDataHelper from '../../../../../../helpers/case-data-helper.ts';
import { ClaimantDefendantPartyType } from '../../../../../../models/claimant-defendant-party-types.ts';
import CCDCaseData from "../../../../../../models/ccd/ccd-case-data.ts";

@AllMethodsStep()
export default class HearingSupportRequirementsFieldDJPage extends ExuiPage(BasePage) {
  private dateFragment: DateFragment;

  async verifyContent(ccdCaseData:CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectSubheading(subheadings.hearingRequirements),
      super.expectLegend(radioButtons.hearingType.label),
      super.expectLabel(radioButtons.hearingType.inPerson.label),
      super.expectLabel(radioButtons.hearingType.video.label),
      super.expectLabel(radioButtons.hearingType.telephone.label),
      super.expectLabel(dropdowns.courtLocation.label),
      super.expectLabel(inputs.telephoneNumber.label),
      super.expectLabel(inputs.email.label),
      super.expectLegend(radioButtons.unavailableDates.label),
      super.expectLegend(radioButtons.support.label),
    ]);
  }

  async selectInPerson() {
    await super.clickBySelector(radioButtons.hearingType.inPerson.selector);
  }

  async selectVideo() {
    await super.clickBySelector(radioButtons.hearingType.video.selector);
  }

  async selectTelephone() {
    await super.clickBySelector(radioButtons.hearingType.telephone.selector);
    await super.inputText(
      `Mr Test Person - ${partys.CLAIMANT_1.key}`,
      inputs.telephoneHearing.selector,
    );
  }

  async enterDetails(claimantPartyType: ClaimantDefendantPartyType) {
    await super.selectFromDropdown(
      preferredCourts[partys.CLAIMANT_1.key].dj,
      dropdowns.courtLocation.selector,
    );
    const claimantData = CaseDataHelper.buildClaimantAndDefendantData(
      partys.CLAIMANT_1,
      claimantPartyType,
    );
    await super.inputText(claimantData.partyPhone, inputs.telephoneNumber.selector);
    await super.inputText(claimantData.partyEmail, inputs.email.selector);
  }

  async selectYesUnavailableDates() {
    await super.clickBySelector(radioButtons.unavailableDates.yes.selector);
    const unavailableDateFrom = DateHelper.addToToday({ months: 1 });
    await this.dateFragment.enterDate(unavailableDateFrom, inputs.unavailableFrom.selectorKey);
    const unavailableDateTo = DateHelper.addToToday({ months: 2 });
    await this.dateFragment.enterDate(unavailableDateTo, inputs.unavailableTo.selectorKey);
  }

  async selectNoUnavailableDates() {
    await super.clickBySelector(radioButtons.unavailableDates.no.selector);
  }

  async selectYesRequireSupport() {
    await super.clickBySelector(radioButtons.support.yes.selector);
    await super.inputText(
      `Mr Test Person requires support - ${partys.CLAIMANT_1.key}`,
      inputs.supportRequirements.selector,
    );
  }

  async selectRequireNoSupport() {
    await super.clickBySelector(radioButtons.support.no.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
