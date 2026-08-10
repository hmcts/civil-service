import BaseDataBuilderFactory from '../../../base/base-data-builder-factory';
import AddCaseNoteDataBuilder from './add-case-note/add-case-note-data-builder';
import AmendPartyDetailsDataBuilder from './amend-party-details/amend-party-details-data-builder';
import ManageContactInformationDataBuilder from '../common/manage-contact-information/manage-contact-information-data-builder';
import MediationUnsuccessfulDataBuilder from './mediation-unsuccessful/mediation-unsuccessful-data-builder';
import TransferOnlineCaseDataBuilder from './transfer-online-case/transfer-online-case-data-builder';

export default class CaseworkerDataBuilderFactory extends BaseDataBuilderFactory {
  get addCaseNoteDataBuilder() {
    return new AddCaseNoteDataBuilder(this.requestsFactory, this.testData);
  }

  get amendPartyDetailsDataBuilder() {
    return new AmendPartyDetailsDataBuilder(this.requestsFactory, this.testData);
  }

  get manageContactInformationDataBuilder() {
    return new ManageContactInformationDataBuilder(this.requestsFactory, this.testData);
  }

  get mediationUnsuccessfulDataBuilder() {
    return new MediationUnsuccessfulDataBuilder(this.requestsFactory, this.testData);
  }

  get transferOnlineCaseDataBuilder() {
    return new TransferOnlineCaseDataBuilder(this.requestsFactory, this.testData);
  }
}
