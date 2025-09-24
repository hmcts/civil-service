import BasePageActionsFactory from '../../../../base/base-page-actions-factory';
import CaseProceedsInCasemanPageFactory from '../../../../pages/exui/caseworker/case-proceeds-in-caseman/case-proceeds-in-caseman-page-factory.ts';
import CaseProceedsInCasemanActions from './case-proceeds-in-caseman-actions.ts';
import ManageDocumentsActions from './manage-documents-actions.ts';
import ManageDocumentsPageFactory from '../../../../pages/exui/caseworker/manage-documents/manage-documents-page-factory.ts';

export default class CaseworkerActionsFactory extends BasePageActionsFactory {
  get caseProceedsInCasemanActions() {
    return new CaseProceedsInCasemanActions(
      new CaseProceedsInCasemanPageFactory(this.page),
      this.testData,
    );
  }

  get manageDocumentsActions() {
    return new ManageDocumentsActions(new ManageDocumentsPageFactory(this.page), this.testData);
  }
}
