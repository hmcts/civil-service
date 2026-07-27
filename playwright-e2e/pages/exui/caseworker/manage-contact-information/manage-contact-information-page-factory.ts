import BasePageFactory from '../../../../base/base-page-factory';
import EventSummaryFragment from '../../fragments/event-summary/event-summary-fragment.ts';
import PartySelectionSpecPage from './party-selection/party-selection-spec-page.ts';
import Applicant1PartyPage from './applicant-1-party/applicant-1-party-page.ts';
import Applicant2PartyPage from './applicant-2-party/applicant-2-party-page.ts';
import Defendant1PartyPage from './defendant-1-party/defendant-1-party-page.ts';
import Defendant2PartyPage from './defendant-2-party/defendant-2-party-page.ts';
import ManageContactInformationSubmitPage from './manage-contact-information-submit/manage-contact-information-submit-page.ts';
import ManageContactInformationConfirmPage from './manage-contact-information-confirm/manage-contact-information-confirm-page.ts';
import PartySelectionPage from './party-selection/party-selection-page.ts';

export default class ManageContactInformationPageFactory extends BasePageFactory {

  get partySelectionPage() {
    return new PartySelectionPage(this.page);
  }

  get partySelectionSpecPage() {
    return new PartySelectionSpecPage(this.page);
  }

  get applicant1PartyPage() {
    return new Applicant1PartyPage(this.page);
  }

  get applicant2PartyPage() {
    return new Applicant2PartyPage(this.page);
  }

  get defendant1PartyPage() {
    return new Defendant1PartyPage(this.page);
  }

  get defendant2PartyPage() {
    return new Defendant2PartyPage(this.page);
  }

  get manageContactInformationSubmitPage() {
    const eventSummaryFragment = new EventSummaryFragment(this.page);
    return new ManageContactInformationSubmitPage(this.page, eventSummaryFragment);
  }

  get manageContactInformationConfirmPage() {
    return new ManageContactInformationConfirmPage(this.page);
  }
}
