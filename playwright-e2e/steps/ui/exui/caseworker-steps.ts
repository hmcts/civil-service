import CaseworkerActionsFactory from '../../../actions/ui/exui/caseworker/caseworker-actions-factory';
import ExuiDashboardActions from '../../../actions/ui/exui/common/exui-dashboard-actions';
import IdamActions from '../../../actions/ui/idam/idam-actions';
import BaseExui from '../../../base/base-exui';
import { AllMethodsStep } from '../../../decorators/test-steps';
import TestData from '../../../models/test-utils/test-data.ts';
import RequestsFactory from '../../../requests/requests-factory';
import { civilAdminUser } from '../../../config/users/exui-users.ts';
import ccdEvents from '../../../constants/ccd-events/ccd-events';

@AllMethodsStep()
export default class CaseworkerSteps extends BaseExui {
  private caseworkerActionsFactory: CaseworkerActionsFactory;

  constructor(
    exuiDashboardActions: ExuiDashboardActions,
    idamActions: IdamActions,
    caseworkerActionsFactory: CaseworkerActionsFactory,
    requestsFactory: RequestsFactory,
    testData: TestData,
  ) {
    super(exuiDashboardActions, idamActions, requestsFactory, testData);
    this.caseworkerActionsFactory = caseworkerActionsFactory;
  }

  async Login() {
    await super.idamActions.exuiLogin(civilAdminUser);
  }

  async CaseProceedsInCaseman() {
    const { caseProceedsInCasemanActions } = this.caseworkerActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await caseProceedsInCasemanActions.caseSettled();
      },
      async () => {},
      ccdEvents.CASE_PROCEEDS_IN_CASEMAN,
      { verifySuccessEvent: false },
    );
  }

  async CaseProceedsInCasemanSpec() {
    const { caseProceedsInCasemanActions } = this.caseworkerActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await caseProceedsInCasemanActions.caseSettledSpec();
      },
      async () => {},
      ccdEvents.CASE_PROCEEDS_IN_CASEMAN,
      { verifySuccessEvent: false },
    );
  }

  async ManageDocuments() {
    const { manageDocumentsActions } = this.caseworkerActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await manageDocumentsActions.addDocuments();
      },
      async () => {},
      ccdEvents.MANAGE_DOCUMENTS,
      { verifySuccessEvent: false },
    );
  }

  async ManageContactInformation() {
    const { manageContactInformationActions } = this.caseworkerActionsFactory;

    await super.retryExuiEvent(
      async () => {
        await manageContactInformationActions.partySelectionClaimant1();
        await manageContactInformationActions.applicantParty1();
        await manageContactInformationActions.manageContactInformationSubmit();
      },
      async () => {
        await manageContactInformationActions.manageContactInformationConfirm();
      },
      ccdEvents.MANAGE_CONTACT_INFORMATION,
      { verifySuccessEvent: false },
    );
  }

  async ManageContactInformationSpec() {
    const { manageContactInformationActions } = this.caseworkerActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await manageContactInformationActions.partySelectionSpecDefendant1();
        await manageContactInformationActions.defendantParty1();
        await manageContactInformationActions.manageContactInformationSubmit();
      },
      async () => {
        await manageContactInformationActions.manageContactInformationConfirm();
      },
      ccdEvents.MANAGE_CONTACT_INFORMATION,
      { verifySuccessEvent: false },
    );
  }

  async MediationUnsuccessful() {
    const { mediationUnsuccessfulActions } = this.caseworkerActionsFactory;
    await super.retryExuiEvent(
      async () => {
        await mediationUnsuccessfulActions.mediationUnsuccessful();
      },
      async () => {},
      ccdEvents.MEDIATION_UNSUCCESSFUL,
    );
  }
}
