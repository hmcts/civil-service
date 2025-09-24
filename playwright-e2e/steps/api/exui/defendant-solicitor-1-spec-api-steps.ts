import BaseApi from '../../../base/base-api';
import { defendantSolicitor1User } from '../../../config/users/exui-users';
import { AllMethodsStep } from '../../../decorators/test-steps';
import CaseRole from '../../../enums/case-role';
import UserAssignedCasesHelper from '../../../helpers/user-assigned-cases-helper';
import TestData from '../../../models/test-data';
import RequestsFactory from '../../../requests/requests-factory';

@AllMethodsStep()
export default class DefendantSolicitor1SpecApiSteps extends BaseApi {
  constructor(requestsFactory: RequestsFactory, testData: TestData) {
    super(requestsFactory, testData);
  }

  async AssignCaseRoleToDefendant1() {
    await this.setupUserData(defendantSolicitor1User);
    const { civilServiceRequests } = this.requestsFactory;
    await civilServiceRequests.assignCaseToDefendant(
      defendantSolicitor1User,
      this.ccdCaseData.id,
      CaseRole.RESPONDENT_SOLICITOR_ONE,
    );
    UserAssignedCasesHelper.addAssignedCaseToUser(defendantSolicitor1User, this.ccdCaseData.id);
  }
}
