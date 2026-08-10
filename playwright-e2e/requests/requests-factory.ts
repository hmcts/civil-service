import BaseRequestsFactory from '../base/base-requests-factory';
import CCDRequests from './ccd-requests';
import CivilServiceRequests from './civil-service-requests';
import GovUKRequests from './gov-uk-requests';
import IdamRequests from './idam-requests';
import WorkAllocationsRequests from './work-allocations-requests';

export default class RequestsFactory extends BaseRequestsFactory {
  get ccdRequests() {
    return new CCDRequests(this.requestContext);
  }

  get idamRequests() {
    return new IdamRequests(this.requestContext);
  }

  get govUKRequests() {
    return new GovUKRequests(this.requestContext);
  }

  get civilServiceRequests() {
    return new CivilServiceRequests(this.requestContext);
  }

  get workAllocationsRequests() {
    return new WorkAllocationsRequests(this.requestContext);
  }
}
