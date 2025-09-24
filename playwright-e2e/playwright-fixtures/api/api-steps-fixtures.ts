import { test as requestFactories } from './request-factory-fixtures';
import { test as dataBuilderFactories } from './data-builder-factory-fixtures';
import { test as testUtils } from '../utils/test-utils-fixtures';
import { mergeTests } from '@playwright/test';
import CaseRoleAssignmentApiSteps from '../../steps/api/case-role-assignment-api-steps';
import IdamApiSteps from '../../steps/api/idam/idam-api-steps';
import ClaimantSolicitorApiSteps from '../../steps/api/exui/claimant-solicitor-api-steps';
import ClaimantSolicitorSpecApiSteps from '../../steps/api/exui/claimant-solicitor-spec-api-steps';
import DefendantSolicitor1ApiSteps from '../../steps/api/exui/defendant-solicitor-1-api-steps';
import DefendantSolicitor1SpecApiSteps from '../../steps/api/exui/defendant-solicitor-1-spec-api-steps';
import DefendantSolicitor2SpecApiSteps from '../../steps/api/exui/defendant-solicitor-2-spec-api-steps';
import DefendantSolicitor2ApiSteps from '../../steps/api/exui/defendant-solicitor-2-api-steps';
import DataApiSteps from '../../steps/api/data-api-steps';

type ApiActionsFixtures = {
  IdamApiSteps: IdamApiSteps;
  DataApiSteps: DataApiSteps;
  ClaimantSolicitorSpecApiSteps: ClaimantSolicitorSpecApiSteps;
  ClaimantSolicitorApiSteps: ClaimantSolicitorApiSteps;
  DefendantSolicitor1SpecApiSteps: DefendantSolicitor1SpecApiSteps;
  DefendantSolicitor1ApiSteps: DefendantSolicitor1ApiSteps;
  DefendantSolicitor2SpecApiSteps: DefendantSolicitor2SpecApiSteps;
  DefendantSolicitor2ApiSteps: DefendantSolicitor2ApiSteps;
  CaseRoleAssignmentApiSteps: CaseRoleAssignmentApiSteps;
};

export const test = mergeTests(testUtils, requestFactories, dataBuilderFactories).extend<ApiActionsFixtures>({
  IdamApiSteps: async ({ _requestsFactory, _testData }, use) => {
    await use(new IdamApiSteps(_requestsFactory, _testData));
  },
  DataApiSteps: async ({ _requestsFactory, _testData }, use) => {
    await use(new DataApiSteps(_requestsFactory, _testData));
  },
  ClaimantSolicitorSpecApiSteps: async ({ _claimantSolicitorDataBuilderFactory, _requestsFactory, _testData }, use) => {
    await use(new ClaimantSolicitorSpecApiSteps(_claimantSolicitorDataBuilderFactory, _requestsFactory, _testData));
  },
  ClaimantSolicitorApiSteps: async ({ _claimantSolicitorDataBuilderFactory, _requestsFactory, _testData }, use) => {
    await use(new ClaimantSolicitorApiSteps(_claimantSolicitorDataBuilderFactory, _requestsFactory, _testData));
  },
  DefendantSolicitor1SpecApiSteps: async ({ _requestsFactory, _testData }, use) => {
    await use(new DefendantSolicitor1SpecApiSteps(_requestsFactory, _testData));
  },
  DefendantSolicitor1ApiSteps: async ({ _requestsFactory, _testData }, use) => {
    await use(new DefendantSolicitor1ApiSteps(_requestsFactory, _testData));
  },
  DefendantSolicitor2SpecApiSteps: async ({ _requestsFactory, _testData }, use) => {
    await use(new DefendantSolicitor2SpecApiSteps(_requestsFactory, _testData));
  },
  DefendantSolicitor2ApiSteps: async ({ _requestsFactory, _testData }, use) => {
    await use(new DefendantSolicitor2ApiSteps(_requestsFactory, _testData));
  },
  CaseRoleAssignmentApiSteps: async ({ _requestsFactory, _testData }, use) => {
    await use(new CaseRoleAssignmentApiSteps(_requestsFactory, _testData));
  }
});
