import BasePage from '../../base/base-page';
import User from '../../models/user';
import { AllMethodsStep } from '../../decorators/test-steps';
import { acceptIdamCookies } from '../../fixtures/cookies/idam-cookies';
import { generateAcceptExuiCookies } from '../../fixtures/cookies/exui-cookies';
import PageError from '../../errors/page-error';
import Cookie from '../../models/cookie';

@AllMethodsStep()
export default class PageCookiesManager extends BasePage {
  async verifyContent(): Promise<void> {
    throw new Error('Method not implemented.');
  }

  async getCookies(): Promise<Cookie[]> {
    return await super.getCookies();
  }

  async cookiesLogin(user: User, cookies: Cookie[]) {
    console.log(
      `Authenticating ${user.name} with email ${user.email} by setting cookies stored in path: ${user.cookiesPath}`,
    );
    await super.addCookies(cookies);
  }

  async addIdamCookies() {
    await super.addCookies(acceptIdamCookies);
  }

  async addExuiCookies({ userId, name }: User) {
    if (!userId) {
      throw new PageError(`UserId for user: ${name} is invalid`);
    }
    await super.addCookies(generateAcceptExuiCookies(userId));
  }

  async cookiesSignOut() {
    await super.clearCookies();
  }
}
