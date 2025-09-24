import BasePage from '../../../../../../base/base-page.ts';
import { claimantSolicitorUser } from '../../../../../../config/users/exui-users.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import ExuiPage from '../../../../exui-page/exui-page.ts';
import { inputs, radioButtons, subheadings } from './notifcations-content.ts';

@AllMethodsStep()
export default class NotificationsPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.expectSubheading(subheadings.notifications),
      super.expectLegend(radioButtons.sameEmailForNotifications.label, { count: 1 }),
      super.expectRadioYesLabel(radioButtons.sameEmailForNotifications.yes.selector),
      super.expectRadioNoLabel(radioButtons.sameEmailForNotifications.no.selector),
    ]);
  }

  async selectYes() {
    await super.clickBySelector(radioButtons.sameEmailForNotifications.yes.selector);
  }

  async selectNo() {
    await super.clickBySelector(radioButtons.sameEmailForNotifications.no.selector);
    await super.expectSubheading(subheadings.notificationDetails);
    await super.expectLabel(inputs.email.label);
    await super.inputText(claimantSolicitorUser.email, inputs.email.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
