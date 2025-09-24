import { lists, paragraphs, subheadings } from './eligibility-spec-content.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import ExuiPage from '../../../../exui-page/exui-page.ts';
import BasePage from '../../../../../../base/base-page.ts';

@AllMethodsStep()
export default class EligibilitySpecPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.verifyHeadings(),
      super.expectSubheading(subheadings.issueCivilCourtProceedings),
      super.expectText(paragraphs.eligibilityCriteria),
      super.expectText(paragraphs.claimantCriteria),
      super.expectText(paragraphs.claimRestrictions),
      super.expectText(lists.issuingClaim),
      super.expectText(lists.addressRequirement),
      super.expectText(lists.paymentRequirement),
      super.expectText(lists.languageRequirement),
      super.expectText(lists.additionalAddressRequirement),
      super.expectText(lists.orderRestriction),
      super.expectText(lists.part8Procedure),
      super.expectText(lists.consumerCreditAct),
      super.expectText(lists.againstCrown),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
