import { test as requestFactories } from './request-factory-fixtures';
import { test as testUtils } from '../utils/test-utils-fixtures';
import ClaimantDefendantSolicitorDataBuilderFactory from '../../data-builders/exui/claimant-defendant-solicitor/claimant-defendant-solicitor-data-builder-factory';
import CaseworkerDataBuilderFactory from '../../data-builders/exui/caseworker/caseworker-data-builder-factory';
import HearingCenterAdminDataBuilderFactory from '../../data-builders/exui/hearing-center-admin/hearing-center-admin-data-builder-factory';
import JudgeLADataBuilderFactory from '../../data-builders/exui/judge-la/judge-la-data-builder-factory';
import { mergeTests } from '@playwright/test';

type DataBuilderFixtures = {
  _claimantDefendantSolicitorDataBuilderFactory: ClaimantDefendantSolicitorDataBuilderFactory;
  _caseworkerDataBuilderFactory: CaseworkerDataBuilderFactory;
  _hearingCenterAdminDataBuilderFactory: HearingCenterAdminDataBuilderFactory;
  _judgeDataBuilderFactory: JudgeLADataBuilderFactory;
};

export const test = mergeTests(testUtils, requestFactories).extend<DataBuilderFixtures>({
  _claimantDefendantSolicitorDataBuilderFactory: async ({ _requestsFactory, _testData }, use) => {
    await use(new ClaimantDefendantSolicitorDataBuilderFactory(_requestsFactory, _testData));
  },
  _caseworkerDataBuilderFactory: async ({ _requestsFactory, _testData }, use) => {
    await use(new CaseworkerDataBuilderFactory(_requestsFactory, _testData));
  },
  _hearingCenterAdminDataBuilderFactory: async ({ _requestsFactory, _testData }, use) => {
    await use(new HearingCenterAdminDataBuilderFactory(_requestsFactory, _testData));
  },
  _judgeDataBuilderFactory: async ({ _requestsFactory, _testData }, use) => {
    await use(new JudgeLADataBuilderFactory(_requestsFactory, _testData));
  },
});
