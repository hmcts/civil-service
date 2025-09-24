import BasePage from '../../../../../../base/base-page';
import {
  claimantSolicitorUser,
  defendantSolicitor2User,
} from '../../../../../../config/users/exui-users';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ExuiPage from '../../../../exui-page/exui-page';
import { paragraphs, subheadings, inputs } from './second-defendant-solicitor-email-spec-content';

@AllMethodsStep()
export default class SecondDefendantSolicitorEmailSpecPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.verifyHeadings(),
      super.expectText(subheadings.emailForDefendantLegalRep),
      super.expectText(paragraphs.emailUsage),
      super.expectText(paragraphs.emailNote),
      super.expectLabel(inputs.email.label),
      super.expectText(inputs.email.hintText),
    ]);
  }

  async enterEmail() {
    await super.inputText(defendantSolicitor2User.email, inputs.email.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
