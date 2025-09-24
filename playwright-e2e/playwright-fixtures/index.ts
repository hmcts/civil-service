import { mergeTests, expect } from '@playwright/test';
import { test as uiSteps } from './ui/ui-steps-fixtures';
import { test as apiSteps } from './api/api-steps-fixtures';
import { expect as pageExpect } from './ui/page-expect-fixtures';

const test = mergeTests(uiSteps, apiSteps);

export { test, pageExpect, expect };
