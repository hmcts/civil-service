import { Page } from '@playwright/test';
import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import DateFragment from '../../../fragments/date/date-fragment';
import {
  radioButtons,
  subheadings,
} from './disposal-hearing-sdo-dj-content';

@AllMethodsStep()
export default class DisposalHearingSdoDJPage extends ExuiPage(BasePage) {
  dateFragment: DateFragment;

  constructor(page: Page, dateFragment: DateFragment) {
    super(page);
    this.dateFragment = dateFragment;
  }

  async verifyContent(ccdCaseData: CCDCaseData): Promise<void> {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData), //split out and specific to this page
      super.expectSubheading(subheadings.orderHearingDetails),
      super.expectText(subheadings.judgementForClaimant),
      super.expectSubheading(subheadings.disclosureOfDocuments),
      super.expectSubheading(subheadings.witnessOfFact),
      super.expectSubheading(subheadings.medicalEvidence),
      super.expectSubheading(subheadings.questionsToExperts),
      super.expectSubheading(subheadings.scheduleOfLoss),
      super.expectSubheading(subheadings.hearingTime),
      super.expectSubheading(subheadings.hearingMethod),
      super.expectSubheading(subheadings.claimSettling),
      super.expectSubheading(subheadings.costs),
      super.expectSubheading(subheadings.welshLanguage),
      super.expectSubheading(subheadings.importantNotes),
      super.expectSubheading(subheadings.newDirection),
    ]);
  }

  async addHearingTimeEstimate() {
    await super.clickBySelector(radioButtons.hearingTime.thirtyMins.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
