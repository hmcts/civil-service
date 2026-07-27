import { test as dataBuilderFactories } from './data-builder-factory-fixtures';
import { test as schemaBuilderFactories } from './schema-builder-factory-fixtures';
import { mergeTests } from '@playwright/test';
import CaseRoleAssignmentApiSteps from '../../steps/api/case-role-assignment-api-steps';
import CaseworkerApiSteps from '../../steps/api/exui/caseworker-api-steps';
import IdamApiSteps from '../../steps/api/idam/idam-api-steps';
import ClaimantSolicitorApiSteps from '../../steps/api/exui/claimant-solicitor-api-steps';
import ClaimantSolicitorSpecApiSteps from '../../steps/api/exui/claimant-solicitor-spec-api-steps';
import DefendantSolicitor1ApiSteps from '../../steps/api/exui/defendant-solicitor-1-api-steps';
import DefendantSolicitor1SpecApiSteps from '../../steps/api/exui/defendant-solicitor-1-spec-api-steps';
import DefendantSolicitor2SpecApiSteps from '../../steps/api/exui/defendant-solicitor-2-spec-api-steps';
import DefendantSolicitor2ApiSteps from '../../steps/api/exui/defendant-solicitor-2-api-steps';
import DataApiSteps from '../../steps/api/data-api-steps';
import HearingCenterAdminApiSteps from '../../steps/api/exui/hearing-center-admin-api-steps';
import JudgeApiSteps from '../../steps/api/exui/judge-api-steps';
import LegalAdvisorApiSteps from '../../steps/api/exui/legal-advisor-api-steps';

type ApiStepsFixtures = {
  IdamApiSteps: IdamApiSteps;
  DataApiSteps: DataApiSteps;
  CaseworkerApiSteps: CaseworkerApiSteps;
  HearingCenterAdminApiSteps: HearingCenterAdminApiSteps;
  JudgeApiSteps: JudgeApiSteps;
  ClaimantSolicitorSpecApiSteps: ClaimantSolicitorSpecApiSteps;
  ClaimantSolicitorApiSteps: ClaimantSolicitorApiSteps;
  DefendantSolicitor1SpecApiSteps: DefendantSolicitor1SpecApiSteps;
  DefendantSolicitor1ApiSteps: DefendantSolicitor1ApiSteps;
  DefendantSolicitor2SpecApiSteps: DefendantSolicitor2SpecApiSteps;
  DefendantSolicitor2ApiSteps: DefendantSolicitor2ApiSteps;
  CaseRoleAssignmentApiSteps: CaseRoleAssignmentApiSteps;
  LegalAdvisorApiSteps: LegalAdvisorApiSteps;
};

export const test = mergeTests(dataBuilderFactories, schemaBuilderFactories).extend<ApiStepsFixtures>({
  IdamApiSteps: async ({ _requestsFactory, _testData }, use) => {
    await use(new IdamApiSteps(_requestsFactory, _testData));
  },
  DataApiSteps: async ({ _requestsFactory, _testData }, use) => {
    await use(new DataApiSteps(_requestsFactory, _testData));
  },
  CaseworkerApiSteps: async ({ _caseworkerDataBuilderFactory, _caseworkerSchemaBuilderFactory, _requestsFactory, _testData }, use) => {
    await use(new CaseworkerApiSteps(_caseworkerDataBuilderFactory, _caseworkerSchemaBuilderFactory, _requestsFactory, _testData));
  },
  HearingCenterAdminApiSteps: async ({ _hearingCenterAdminDataBuilderFactory, _hearingCenterAdminSchemaBuilderFactory, _requestsFactory, _testData }, use) => {
    await use(new HearingCenterAdminApiSteps(_hearingCenterAdminDataBuilderFactory, _hearingCenterAdminSchemaBuilderFactory, _requestsFactory, _testData));
  },
  JudgeApiSteps: async ({ _judgeDataBuilderFactory, _judgeSchemaBuilderFactory, _requestsFactory, _testData }, use) => {
    await use(new JudgeApiSteps(_judgeDataBuilderFactory, _judgeSchemaBuilderFactory, _requestsFactory, _testData));
  },
  ClaimantSolicitorSpecApiSteps: async ({ _claimantDefendantSolicitorDataBuilderFactory, _claimantDefendantSolicitorSchemaBuilderFactory, _requestsFactory, _testData }, use) => {
    await use(new ClaimantSolicitorSpecApiSteps(_claimantDefendantSolicitorDataBuilderFactory, _claimantDefendantSolicitorSchemaBuilderFactory, _requestsFactory, _testData));
  },
  ClaimantSolicitorApiSteps: async ({ _claimantDefendantSolicitorDataBuilderFactory, _claimantDefendantSolicitorSchemaBuilderFactory, _requestsFactory, _testData }, use) => {
    await use(new ClaimantSolicitorApiSteps(_claimantDefendantSolicitorDataBuilderFactory, _claimantDefendantSolicitorSchemaBuilderFactory, _requestsFactory, _testData));
  },
  DefendantSolicitor1SpecApiSteps: async ({ _claimantDefendantSolicitorDataBuilderFactory, _claimantDefendantSolicitorSchemaBuilderFactory, _requestsFactory, _testData }, use) => {
    await use(new DefendantSolicitor1SpecApiSteps(_claimantDefendantSolicitorDataBuilderFactory, _claimantDefendantSolicitorSchemaBuilderFactory, _requestsFactory, _testData));
  },
  DefendantSolicitor1ApiSteps: async ({ _claimantDefendantSolicitorDataBuilderFactory, _claimantDefendantSolicitorSchemaBuilderFactory, _requestsFactory, _testData }, use) => {
    await use(new DefendantSolicitor1ApiSteps(_claimantDefendantSolicitorDataBuilderFactory, _claimantDefendantSolicitorSchemaBuilderFactory, _requestsFactory, _testData));
  },
  DefendantSolicitor2SpecApiSteps: async ({ _claimantDefendantSolicitorDataBuilderFactory, _claimantDefendantSolicitorSchemaBuilderFactory, _requestsFactory, _testData }, use) => {
    await use(new DefendantSolicitor2SpecApiSteps(_claimantDefendantSolicitorDataBuilderFactory, _claimantDefendantSolicitorSchemaBuilderFactory, _requestsFactory, _testData));
  },
  DefendantSolicitor2ApiSteps: async ({ _claimantDefendantSolicitorDataBuilderFactory, _claimantDefendantSolicitorSchemaBuilderFactory, _requestsFactory, _testData }, use) => {
    await use(new DefendantSolicitor2ApiSteps(_claimantDefendantSolicitorDataBuilderFactory, _claimantDefendantSolicitorSchemaBuilderFactory, _requestsFactory, _testData));
  },
  CaseRoleAssignmentApiSteps: async ({ _requestsFactory, _testData }, use) => {
    await use(new CaseRoleAssignmentApiSteps(_requestsFactory, _testData));
  },
  LegalAdvisorApiSteps: async ({ _judgeDataBuilderFactory, _judgeSchemaBuilderFactory, _requestsFactory, _testData }, use) => {
    await use(new LegalAdvisorApiSteps(_judgeDataBuilderFactory, _judgeSchemaBuilderFactory, _requestsFactory, _testData));
  }
});
