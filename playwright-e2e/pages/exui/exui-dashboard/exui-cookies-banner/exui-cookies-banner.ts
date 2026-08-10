import BasePage from '../../../../base/base-page';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import { buttons, heading, paragraphs } from './exui-cookies-banner-content';

@AllMethodsStep()
export default class ExuiCookiesBanner extends BasePage {
  async verifyContent(): Promise<void> {
    await super.runVerifications([
      super.expectSubheading(heading),
      super.expectText(paragraphs.cookiesDescription1),
      super.expectText(paragraphs.cookiesDescription2),
    ]);
  }

  async acceptCookies() {
    if (await super.selectorExists(buttons.accept.selector)) {
      await super.clickBySelector(buttons.accept.selector);
    }
  }

  async rejectCookies() {
    if (await super.selectorExists(buttons.reject.selector)) {
      await super.clickBySelector(buttons.reject.selector);
    }
  }
}
