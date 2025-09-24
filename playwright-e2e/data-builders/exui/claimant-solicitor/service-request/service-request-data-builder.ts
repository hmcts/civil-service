import BaseDataBuilder from '../../../../base/base-data-builder';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import { serviceUpdateDTO } from './service-request-data-builder-components';

@AllMethodsStep()
export default class ServiceRequestDataBuilder extends BaseDataBuilder {
  async buildPaidServiceRequestDTO(caseId: number, paymentStatus: string) {
    return this.buildData(caseId, paymentStatus);
  }

  protected async buildData(caseId: number, paymentStatus: string) {
    return {
      ...serviceUpdateDTO(caseId, paymentStatus),
    };
  }
}
