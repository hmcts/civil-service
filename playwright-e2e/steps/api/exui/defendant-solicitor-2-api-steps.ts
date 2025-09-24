import BaseApi from '../../../base/base-api';
import { defendantSolicitor2User } from '../../../config/users/exui-users';
import { AllMethodsStep } from '../../../decorators/test-steps';
import CaseRole from '../../../enums/case-role';
import UserAssignedCasesHelper from '../../../helpers/user-assigned-cases-helper';
import TestData from '../../../models/test-data';
import RequestsFactory from '../../../requests/requests-factory';

@AllMethodsStep()
export default class DefendantSolicitor2ApiSteps extends BaseApi {
  constructor(requestsFactory: RequestsFactory, testData: TestData) {
    super(requestsFactory, testData);
  }

  async AssignCaseRole() {
    await this.setupUserData(defendantSolicitor2User);
    const { civilServiceRequests } = this.requestsFactory;
    await civilServiceRequests.assignCaseToDefendant(
      defendantSolicitor2User,
      this.ccdCaseData.id,
      CaseRole.RESPONDENT_SOLICITOR_TWO,
    );
    UserAssignedCasesHelper.addAssignedCaseToUser(defendantSolicitor2User, this.ccdCaseData.id);
  }
}
