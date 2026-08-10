import BasePageFactory from '../../../../base/base-page-factory';
import DateFragment from '../../fragments/date/date-fragment';
import ConfirmInformAgreedExtensionDateSpecPage from './lr-spec/confirm-inform-agreed-extension-date-spec/confirm-inform-agreed-extension-date-spec-page';
import ExtensionDateSpecPage from './lr-spec/extension-date-spec/extension-date-spec-page';
import ConfirmInformAgreedExtensionDatePage from './unspec/confirm-inform-agreed-extension-date/confirm-inform-agreed-extension-date-page';
import ExtensionDatePage from './unspec/extension-date/extension-date-page';

export default class InformAgreedExtensionDatePageFactory extends BasePageFactory {
  get extensionDateSpecPage() {
    const dateFragment = new DateFragment(this.page);
    return new ExtensionDateSpecPage(this.page, dateFragment);
  }

  get confirmInformAgreedExtensionDateSpecPage() {
    return new ConfirmInformAgreedExtensionDateSpecPage(this.page);
  }

  get extensionDatePage() {
    return new ExtensionDatePage(this.page);
  }

  get confirmInformAgreedExtensionDatePage() {
    return new ConfirmInformAgreedExtensionDatePage(this.page);
  }
}
