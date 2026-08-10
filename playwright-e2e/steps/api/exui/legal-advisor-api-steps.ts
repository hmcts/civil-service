import BaseApi from '../../../base/base-api';
import { tribunalCaseworkerRegion1User } from '../../../config/users/exui-users';
import CaseState from '../../../constants/cases/case-state';
import ccdEvents from '../../../constants/ccd-events/ccd-events';
import legalAdvisorSmallClaimsTrackDirectionsTask from '../../../constants/wa-tasks/legalAdvisorSmallClaimsTrackDirectionsTask';
import { AllMethodsStep } from '../../../decorators/test-steps';
import ZodHelper from '../../../helpers/zod-helper';
import TestData from '../../../models/test-utils/test-data';
import RequestsFactory from '../../../requests/requests-factory';
import JudgeLADataBuilderFactory from '../../../data-builders/exui/judge-la/judge-la-data-builder-factory';
import JudgeLASchemaBuilderFactory from '../../../schema-builders/exui/judge-la/judge-la-schema-builder-factory';

@AllMethodsStep()
export default class LegalAdvisorApiSteps extends BaseApi {
  private judgeDataBuilderFactory: JudgeLADataBuilderFactory;
  private judgeSchemaBuilderFactory: JudgeLASchemaBuilderFactory;

  constructor(
    judgeDataBuilderFactory: JudgeLADataBuilderFactory,
    judgeSchemaBuilderFactory: JudgeLASchemaBuilderFactory,
    requestsFactory: RequestsFactory,
    testData: TestData,
  ) {
    super(requestsFactory, testData);
    this.judgeDataBuilderFactory = judgeDataBuilderFactory;
    this.judgeSchemaBuilderFactory = judgeSchemaBuilderFactory;
  }

  async SdoSmallTrackSum() {
    await this.setupApiStep(tribunalCaseworkerRegion1User);
    const taskId = await super.retrieveAndAssignWATask(
      tribunalCaseworkerRegion1User,
      legalAdvisorSmallClaimsTrackDirectionsTask,
    );
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { createSdoDataBuilder } = this.judgeDataBuilderFactory;
    const createSdoData = await createSdoDataBuilder.buildSmallSumSdo();

    await super.submitCCDEvent(
      tribunalCaseworkerRegion1User,
      ccdEvents.CREATE_SDO,
      createSdoData,
      CaseState.CASE_PROGRESSION,
    );
    await this.completeWATask(tribunalCaseworkerRegion1User, taskId);

    const { createSdoSchemaBuilder } = this.judgeSchemaBuilderFactory;
    const createSdoSchema =
      await createSdoSchemaBuilder.buildSmallSumSdo(caseDataBeforeSubmission);
    ZodHelper.safeParse(createSdoSchema, this.ccdCaseData);
  }

  async SdoSmallTrackNoSum() {
    await this.setupApiStep(tribunalCaseworkerRegion1User);
    const taskId = await super.retrieveAndAssignWATask(
      tribunalCaseworkerRegion1User,
      legalAdvisorSmallClaimsTrackDirectionsTask,
    );
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { createSdoDataBuilder } = this.judgeDataBuilderFactory;
    const createSdoData = await createSdoDataBuilder.buildSmallNoSumSdo();

    await super.submitCCDEvent(
      tribunalCaseworkerRegion1User,
      ccdEvents.CREATE_SDO,
      createSdoData,
      CaseState.CASE_PROGRESSION,
    );
    await this.completeWATask(tribunalCaseworkerRegion1User, taskId);

    const { createSdoSchemaBuilder } = this.judgeSchemaBuilderFactory;
    const createSdoSchema =
      await createSdoSchemaBuilder.buildSmallNoSumSdo(caseDataBeforeSubmission);
    ZodHelper.safeParse(createSdoSchema, this.ccdCaseData);
  }

  async SdoSmallTrackSumDRH() {
    await this.setupApiStep(tribunalCaseworkerRegion1User);
    const taskId = await super.retrieveAndAssignWATask(
      tribunalCaseworkerRegion1User,
      legalAdvisorSmallClaimsTrackDirectionsTask,
    );
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { createSdoDataBuilder } = this.judgeDataBuilderFactory;
    const createSdoData = await createSdoDataBuilder.buildSmallSumDRHSdo();

    await super.submitCCDEvent(
      tribunalCaseworkerRegion1User,
      ccdEvents.CREATE_SDO,
      createSdoData,
      CaseState.CASE_PROGRESSION,
    );
    await this.completeWATask(tribunalCaseworkerRegion1User, taskId);

    const { createSdoSchemaBuilder } = this.judgeSchemaBuilderFactory;
    const createSdoSchema =
      await createSdoSchemaBuilder.buildSmallSumDRHSdo(caseDataBeforeSubmission);
    ZodHelper.safeParse(createSdoSchema, this.ccdCaseData);
  }

  async SdoSmallTrackNoSumDRH() {
    await this.setupApiStep(tribunalCaseworkerRegion1User);
    const taskId = await super.retrieveAndAssignWATask(
      tribunalCaseworkerRegion1User,
      legalAdvisorSmallClaimsTrackDirectionsTask,
    );
    const caseDataBeforeSubmission = structuredClone(this.ccdCaseData);

    const { createSdoDataBuilder } = this.judgeDataBuilderFactory;
    const createSdoData = await createSdoDataBuilder.buildSmallNoSumDRHSdo();

    await super.submitCCDEvent(
      tribunalCaseworkerRegion1User,
      ccdEvents.CREATE_SDO,
      createSdoData,
      CaseState.CASE_PROGRESSION,
    );
    await this.completeWATask(tribunalCaseworkerRegion1User, taskId);

    const { createSdoSchemaBuilder } = this.judgeSchemaBuilderFactory;
    const createSdoSchema =
      await createSdoSchemaBuilder.buildSmallNoSumDRHSdo(caseDataBeforeSubmission);
    ZodHelper.safeParse(createSdoSchema, this.ccdCaseData);
  }
}
