import BaseDataBuilder from '../../../../base/base-data-builder';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import { serviceUpdateDTO } from './service-request-data-builder-components';

@AllMethodsStep()
export default class ServiceRequestDataBuilder extends BaseDataBuilder {
  async buildPaidServiceRequestDTO(paymentStatus: string, caseId?: number) {
    return this.buildData(paymentStatus, caseId);
  }

  protected async buildData(paymentStatus: string, caseId?: number) {
    return {
      ...serviceUpdateDTO(paymentStatus, caseId),
    };
  }
}
