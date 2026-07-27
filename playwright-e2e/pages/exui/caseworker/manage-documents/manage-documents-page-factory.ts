import BasePageFactory from '../../../../base/base-page-factory';
import EventSummaryFragment from '../../fragments/event-summary/event-summary-fragment.ts';
import ManageDocumentsSubmitPage from '../manage-documents/manage-documents-submit/manage-documents-submit-page.ts';
import ManageDocumentsPage from './manage-documents/manage-documents-page.ts';

export default class ManageDocumentsPageFactory extends BasePageFactory {
  get manageDocumentsPage() {
    return new ManageDocumentsPage(this.page);
  }

  get manageDocumentsSubmitPage() {
    const eventSummaryFragment = new EventSummaryFragment(this.page);
    return new ManageDocumentsSubmitPage(this.page, eventSummaryFragment);
  }
}
