import { test as base } from '@playwright/test';
import ClaimantSolicitorDataBuilderFactory from '../../data-builders/exui/claimant-solicitor/claimant-solicitor-data-builder-factory';

type DataBuilderFixtures = {
  _claimantSolicitorDataBuilderFactory: ClaimantSolicitorDataBuilderFactory;
};

export const test = base.extend<DataBuilderFixtures>({
  _claimantSolicitorDataBuilderFactory: async ({}, use) => {
    await use(new ClaimantSolicitorDataBuilderFactory());
  }
});
