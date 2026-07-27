import BasePageFactory from '../../../../base/base-page-factory';
import FinalOrderPreviewPage from './final-order-preview/final-order-preview-page';
import FinalOrderSelectPage from './final-order-select/final-order-select-page';
import FreeFormOrderPage from './free-form-order/free-form-order-page';
import SubmitGenerateDirectionsOrderPage from './submit-generate-directions-order/submit-generate-directions-order-page';
import ConfirmGenerateDirectionsOrderPage from './confirm-generate-directions-order/confirm-generate-directions-order-page';

export default class GenerateDirectionsOrderPageFactory extends BasePageFactory {
  get finalOrderSelectPage() {
    return new FinalOrderSelectPage(this.page);
  }

  get freeFormOrderPage() {
    return new FreeFormOrderPage(this.page);
  }

  get finalOrderPreviewPage() {
    return new FinalOrderPreviewPage(this.page);
  }

  get submitGenerateDirectionsOrderPage() {
    return new SubmitGenerateDirectionsOrderPage(this.page);
  }

  get confirmGenerateDirectionsOrderPage() {
    return new ConfirmGenerateDirectionsOrderPage(this.page);
  }
}
