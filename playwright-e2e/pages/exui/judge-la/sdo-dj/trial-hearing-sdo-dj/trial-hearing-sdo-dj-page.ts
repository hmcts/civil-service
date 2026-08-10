import { Page } from '@playwright/test';
import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import DateFragment from '../../../fragments/date/date-fragment';
import {
  radioButtons,
  subheadings,
} from './trial-hearing-sdo-dj-content';

@AllMethodsStep()
export default class TrialHearingSdoDJPage extends ExuiPage(BasePage) {
  dateFragment: DateFragment;

  constructor(page: Page, dateFragment: DateFragment) {
    super(page);
    this.dateFragment = dateFragment;
  }

  async verifyContent(ccdCaseData: CCDCaseData): Promise<void> {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectHeading(subheadings.orderHearingDetails),
      super.expectSubheading(subheadings.judgesRecital),
      super.expectText(subheadings.judgementForClaimant),
      super.expectText(subheadings.allocation),
      super.expectSubheading(subheadings.alternativeDisputeResolution),
      super.expectSubheading(subheadings.variationOfDirections),
      super.expectSubheading(subheadings.settlement),
      super.expectSubheading(subheadings.disclosureOfDocuments),
      super.expectSubheading(subheadings.witnessOfFact),
      super.expectSubheading(subheadings.scheduleOfLoss),
      super.expectSubheading(subheadings.costs),
      super.expectSubheading(subheadings.hearingTime),
      super.expectSubheading(subheadings.hearingMethod),
      super.expectSubheading(subheadings.hearingNotes),
      super.expectSubheading(subheadings.welshLanguage),
      super.expectSubheading(subheadings.importantNotes),
      super.expectSubheading(subheadings.buildingDispute),
      super.expectSubheading(subheadings.clinicalNegligence),
      super.expectSubheading(subheadings.newDirection),
      super.expectSubheading(subheadings.creditHire),
      super.expectSubheading(subheadings.employersLiability),
      super.expectSubheading(subheadings.housingDisrepair),
      super.expectSubheading(subheadings.expertEvidence),
      super.expectSubheading(subheadings.roadTrafficAccident),
      super.expectSubheading(subheadings.newDirection),
    ]);
  }

  async addHearingTimeEstimate() {
    await super.clickBySelector(radioButtons.hearingTime.oneHour.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
