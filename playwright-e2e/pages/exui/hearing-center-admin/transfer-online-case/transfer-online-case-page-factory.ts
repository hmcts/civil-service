import BasePageFactory from '../../../../base/base-page-factory.ts';
import TransferOnlineCasePage from './transfer-online-case/transfer-online-case-page.ts';
import TransferOnlineCaseSubmitPage from './transfer-online-case-submit/transfer-online-case-submit-page.ts';
import TransferOnlineCaseConfirmPage from './transfer-online-case-confirm/transfer-online-case-confirm-page.ts';

export default class TransferOnlineCasePageFactory extends BasePageFactory {
  get transferOnlineCasePage() {
    return new TransferOnlineCasePage(this.page);
  }

  get transferOnlineCaseSubmitPage() {
    return new TransferOnlineCaseSubmitPage(this.page);
  }

  get transferOnlineCaseConfirmPage() {
    return new TransferOnlineCaseConfirmPage(this.page);
  }
}
