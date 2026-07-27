import BasePageFactory from '../../base/base-page-factory';
import CreateAccountPage from './create-account/create-account-page';
import IdamCookiesBanner from './idam-cookies-banner.ts/idam-cookies-banner';
import LoginPage from './login/login-page';

export default class IdamPageFactory extends BasePageFactory {
  get loginPage() {
    return new LoginPage(this.page);
  }

  get idamsCookiesBanner() {
    return new IdamCookiesBanner(this.page);
  }

  get createAccountPage() {
    return new CreateAccountPage(this.page);
  }
}
