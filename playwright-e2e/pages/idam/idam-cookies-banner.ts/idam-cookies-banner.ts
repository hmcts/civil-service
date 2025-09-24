import BasePage from '../../../base/base-page';
import { AllMethodsStep } from '../../../decorators/test-steps';
import { buttons, heading, paragraphs } from './idam-cookies-banner-content';

@AllMethodsStep()
export default class IdamCookiesBanner extends BasePage {
  async verifyContent(): Promise<void> {
    await super.runVerifications([
      super.expectSubheading(heading),
      super.expectText(paragraphs.cookiesDescription1),
      super.expectText(paragraphs.cookiesDescription2),
    ]);
  }

  async acceptCookies() {
    await super.clickBySelector(buttons.accept.selector);
    await super.expectText(paragraphs.acceptedCookiesMessage);
    await super.clickBySelector(buttons.hideMessage.selector);
  }

  async rejectCookies() {
    await super.clickBySelector(buttons.reject.selector);
    await super.expectText(paragraphs.rejectedCookiesMessage);
    await super.clickBySelector(buttons.hideMessage.selector);
  }
}
