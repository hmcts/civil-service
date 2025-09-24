import BasePage from '../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import ExuiPage from '../../../../exui-page/exui-page.ts';
import { heading, subheadings, paragraphs, radioButtons, inputs } from './hearing-type-content.ts';
import CCDCaseData from '../../../../../../models/ccd/ccd-case-data.ts';
import partys from '../../../../../../constants/partys.ts';
import {getFormattedCaseId} from "../../../../exui-page/exui-content.ts";

@AllMethodsStep()
export default class HearingTypePage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.expectHeading(heading),
      super.expectHeading(getFormattedCaseId(ccdCaseData.id), {exact: false}),
      super.expectHeading(ccdCaseData.caseNamePublic, {exact:false}),
      super.expectSubheading(subheadings.hearingType),
      super.expectText(paragraphs.descriptionText),
      super.expectLegend(radioButtons.hearingType.label)
    ]);
  }

  async selectDisposalHearing() {
    await super.clickBySelector(radioButtons.hearingType.disposalHearing.selector);
    await super.expectSubheading(subheadings.draftOrderDirections);
    await super.expectText(paragraphs.descriptionDisposalHearing);
    await super.inputText(`Test directions - ${partys.CLAIMANT_1.key}`, inputs.draftOrder.selector);
  }

  async selectTrial() {
    await super.clickBySelector(radioButtons.hearingType.trial.selector);
    await super.runVerifications([
      super.expectText(subheadings.draftOrderDirections),
      super.expectText(paragraphs.descriptionTrial),
    ]);
    await super.inputText(`Test directions - ${partys.CLAIMANT_1.key}`, inputs.draftOrder.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
