import BasePageFactory from '../../../../base/base-page-factory';
import partys from '../../../../constants/partys';
import AddAnotherClaimantPage from './common/add-another-claimant/add-another-claimant-page';
import AddAnotherDefendantPage from './common/add-another-defendant/add-another-defendant-page';
import ClaimantSolicitorOrganisationPage from './common/claimant-solicitor-organisation/claimant-solicitor-organisation-page';
import ClaimantPage from './common/claimant/claimant-page';
import DefendantPage from './common/defendant/defendant-page';
import NotificationsPage from './common/notifications/notifcations-page';
import PbaNumberPage from './common/pba-number/pba-number-page';
import ReferencesPage from './common/references/references-page';
import SameLegalRepresentativePage from './common/same-legal-representative/same-legal-representative-page';
import SecondClaimantPage from './common/second-claimant/second-claimant-page';
import SecondDefendantPage from './common/second-defendant/second-defendant-page';
import StatementOfTruthCreateClaimPage from './common/statement-of-truth-create-claim/statement-of-truth-create-claim-page';
import SubmitCreateClaimPage from './common/submit-create-claim/submit-create-claim-page';
import BreakDownInterestPage from './lr-spec/break-down-interest/break-down-interest-page';
import ChecklistPage from './lr-spec/check-list/checklist-page';
import ClaimAmountDetailsPage from './lr-spec/claim-amount-details/claim-amount-details-page';
import ClaimAmountPage from './lr-spec/claim-amount/claim-amount-page';
import ClaimInterestOptionsPage from './lr-spec/claim-interest-options/claim-interest-options-page';
import ClaimInterestPage from './lr-spec/claim-interest/claim-interest-page';
import ClaimTimelineUploadPage from './lr-spec/claim-timeline-upload/claim-timeline-upload-page';
import ClaimTimelinePage from './lr-spec/claim-timeline/claim-timeline-page';
import ConfirmCreateClaimSpecPage from './lr-spec/confirm-create-claim-spec/confirm-create-claim-spec-page';
import DefendantSolicitorEmailSpecPage from './lr-spec/defendant-solicitor-email-spec/defendant-solicitor-email-spec-page';
import DefendantSolicitorOrganisationSpecPage from './lr-spec/defendant-solicitor-organisation-spec/defendant-solicitor-organisation-spec-page';
import DetailsSpecPage from './lr-spec/details-spec/details-spec-page';
import EligibilitySpecPage from './lr-spec/eligibilty-spec/eligibility-spec-page';
import EvidenceListPage from './lr-spec/evidence-list/evidence-list-page';
import FixedCommencementCostsPage from './lr-spec/fixed-commencement-costs/fixed-commencement-costs-page';
import FlightDelayClaimPage from './lr-spec/flight-delay-claim/flight-delay-claim-page';
import InterestClaimFromPage from './lr-spec/interest-claim-from/interest-claim-from-page';
import InterestClaimUntilPage from './lr-spec/interest-claim-until/interest-claim-until-page';
import InterestFromSpecificDate from './lr-spec/interest-from-specific-date/interest-from-specific-date-page';
import InterestSummaryPage from './lr-spec/interest-summary/interest-summary-page';
import LegalRepresentationRespondent2Page from './lr-spec/legal-representation-respondent-2/legal-representation-respondent-2-page';
import LegalRepresentationSpecPage from './lr-spec/legal-representation-spec/legal-representation-spec-page';
import SpecRespondent2CorrespondenceAddressPage from './lr-spec/spec-respondent-2-correspondence-address/spec-respondent-2-correspondence-address-page';
import SameRateInterestSelectionPage from './lr-spec/same-rate-interest-selection/same-rate-interest-selection-page';
import SecondDefendantSolicitorEmailSpecPage from './lr-spec/second-defendant-solicitor-email-spec/second-defendant-solicitor-email-spec-page';
import SecondDefendantSolicitorOrganisationSpecPage from './lr-spec/second-defendant-solicitor-organisation-spec/second-defendant-solicitor-organisation-spec-page';
import SpecCorrespondenceAddressPage from './lr-spec/spec-correspondence-address/spec-correspondence-address-page';
import SpecRespondentCorrespondenceAddressPage from './lr-spec/spec-respondent-correspondence-address/spec-respondent-correspondence-address-page';
import UnRegisteredDefendantSolicitorOrganisationPage from './lr-spec/unregistered-defendant-solicitor-organisation/unregistered-defendant-solicitor-organisation-page';
import UnregisteredSecondDefendantSolicitorOrganisationPage from './lr-spec/unregistered-second-defendant-solicitor-organisation/unregistered-second-defendant-solicitor-organisation-page';
import UploadClaimDocumentPage from './lr-spec/upload-claim-document/upload-claim-document-page';
import ClaimTypePage from './unspec/claim-type/claim-type-page';
import ClaimValuePage from './unspec/claim-value/claim-value-page';
import ClaimantLitigationFriendPage from './unspec/claimant-litigation-friend/claimant-litigation-friend-page';
import ClaimantSolicitorServiceAddressPage from './unspec/claimant-solicitor-service-address/claimant-solicitor-service-address-page';
import ConfirmCreateClaimPage from './unspec/confirm-create-claim/confirm-create-claim-page';
import ConfirmCreateClaimLIPPage from './unspec/confirm-create-claim-LIP/confirm-create-claim-LIP-page';
import CourtPage from './unspec/court/court-page';
import DefendantSolicitorEmailPage from './unspec/defendant-solicitor-email/defendant-solicitor-email-page';
import DefendantSolicitorOrganisationPage from './unspec/defendant-solicitor-organisation/defendant-solicitor-organisation-page';
import DefendantSolicitorServiceAddressPage from './unspec/defendant-solicitor-service-address/defendant-solicitor-service-address-page';
import DetailsPage from './unspec/details/details-page';
import EligibilityPage from './unspec/eligibility/eligibility-page';
import LegalRepresentationPage from './unspec/legal-representation/legal-representation-page';
import PersonalInjuryTypePage from './unspec/personal-injury-type/personal-injury-type-page';
import SecondClaimantLitigationFriendPage from './unspec/second-claimant-litigation-friend/second-claimant-litigation-friend-page';
import SecondDefendantLegalRepresentationPage from './unspec/second-defendant-legal-representation/second-defendant-legal-representation-page';
import SecondDefendantSolicitorEmailPage from './unspec/second-defendant-solicitor-email/second-defendant-solicitor-email-page';
import SecondDefendantSolicitorOrganisationPage from './unspec/second-defendant-solicitor-organisation/second-defendant-solicitor-organisation-page';
import SecondDefendantSolicitorReferencePage from './unspec/second-defendant-solicitor-reference/second-defendant-solicitor-reference-page';
import SecondDefendantSolicitorServiceAddressPage from './unspec/second-defendant-solicitor-service-address/second-defendant-solicitor-service-address-page';
import UploadCreateClaimPage from './unspec/upload-create-claim/upload-create-claim-page';
import UploadParticularsOfClaimPage from './unspec/upload-particulars-of-claim/upload-particulars-of-claim-page';
import CorrespondenceAddressFragment from '../../fragments/correspondence-address/correspondence-address-fragment';
import DateFragment from '../../fragments/date/date-fragment';
import LitigationFriendFragment from '../../fragments/litigation-friend/litigation-friend-fragment';
import OrganisationRegisteredFragment from '../../fragments/organisation-registered/organisation-registered-fragment';
import OrganisationFragment from '../../fragments/organisation/organisation-fragment';
import ParticularsOfClaimFragment from '../../fragments/particulars-of-claim/particulars-of-claim-fragment';
import RemoteHearingFragment from '../../fragments/remote-hearing/remote-hearing-fragment';
import ServiceAddressFragment from '../../fragments/service-address/service-address-fragment';
import SolicitorReferenceFragment from '../../fragments/solicitor-reference/solicitor-reference-fragment';
import StatementOfTruthFragment from '../../fragments/statement-of-truth/statement-of-truth-fragment';
import UnregisteredOrganisationAddressFragment from '../../fragments/unregistered-organisation-address/unregistered-organisation-address-fragment';
import UnregisteredOrganisationFragment from '../../fragments/unregistered-organisation/unregistered-organisation-fragment';
import YesOrNoFragment from '../../fragments/yes-or-no/yes-or-no-fragment';

export default class CreateClaimPageFactory extends BasePageFactory {
  get checkListPage() {
    return new ChecklistPage(this.page);
  }

  get eligibilityPage() {
    return new EligibilityPage(this.page);
  }

  get eligibilitySpecPage() {
    return new EligibilitySpecPage(this.page);
  }

  get referencesPage() {
    const claimantSolicitorReferenceFragment = new SolicitorReferenceFragment(
      this.page,
      partys.CLAIMANT_1,
      partys.CLAIMANT_SOLICITOR_1,
    );
    const defendantSolicitorReferenceFragment = new SolicitorReferenceFragment(
      this.page,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
    );
    return new ReferencesPage(
      this.page,
      claimantSolicitorReferenceFragment,
      defendantSolicitorReferenceFragment,
    );
  }

  get courtPage() {
    const remoteHearingFragment = new RemoteHearingFragment(this.page, partys.CLAIMANT_1);
    return new CourtPage(this.page, remoteHearingFragment);
  }

  get notificationsPage() {
    return new NotificationsPage(this.page);
  }

  get claimantPage() {
    return new ClaimantPage(this.page);
  }

  get addAnotherClaimantPage() {
    const yesOrNoFragment = new YesOrNoFragment(this.page);
    return new AddAnotherClaimantPage(this.page, yesOrNoFragment);
  }

  get secondClaimantPage() {
    return new SecondClaimantPage(this.page);
  }

  get secondClaimantLitigationFriendPage() {
    return new SecondClaimantLitigationFriendPage(this.page);
  }

  get claimantLitigationFriendPage() {
    const litigationFriendFragment = new LitigationFriendFragment(
      this.page,
      partys.CLAIMANT_1_LITIGATION_FRIEND,
    );
    return new ClaimantLitigationFriendPage(this.page, litigationFriendFragment);
  }

  get claimantSolicitorOrganisationPage() {
    const organisationFragment = new OrganisationFragment(this.page, partys.CLAIMANT_1);
    return new ClaimantSolicitorOrganisationPage(this.page, organisationFragment);
  }

  get claimantSolicitorServiceAddressPage() {
    const serviceAddressFragment = new ServiceAddressFragment(
      this.page,
      partys.CLAIMANT_1,
      partys.CLAIMANT_SOLICITOR_1,
    );
    return new ClaimantSolicitorServiceAddressPage(this.page, serviceAddressFragment);
  }

  get specCorrespondenceAddressPage() {
    const correspondenceAddressFragment = new CorrespondenceAddressFragment(
      this.page,
      partys.CLAIMANT_SOLICITOR_1,
      partys.CLAIMANT_1,
    );
    return new SpecCorrespondenceAddressPage(this.page, correspondenceAddressFragment);
  }

  get defendantPage() {
    return new DefendantPage(this.page);
  }

  get legalRepresentationPage() {
    return new LegalRepresentationPage(this.page);
  }

  get legalRepresentationSpecPage() {
    return new LegalRepresentationSpecPage(this.page);
  }

  get defendantSolicitorOrganisationPage() {
    const organisationFragment = new OrganisationFragment(this.page, partys.DEFENDANT_1);
    return new DefendantSolicitorOrganisationPage(this.page, organisationFragment);
  }

  get defendantSolicitorOrganisationSpecPage() {
    const organisationRegisteredFragment = new OrganisationRegisteredFragment(
      this.page,
      partys.DEFENDANT_1,
    );
    const organisationFragment = new OrganisationFragment(this.page, partys.DEFENDANT_1);
    return new DefendantSolicitorOrganisationSpecPage(
      this.page,
      organisationRegisteredFragment,
      organisationFragment,
    );
  }

  get unregisteredDefendantSolicitorOrganisationPage() {
    const unregisteredOrganisationFragment = new UnregisteredOrganisationFragment(
      this.page,
      partys.DEFENDANT_SOLICITOR_1,
    );
    const unregisteredOrganisationAddressFragment = new UnregisteredOrganisationAddressFragment(
      this.page,
      partys.DEFENDANT_SOLICITOR_1,
    );
    return new UnRegisteredDefendantSolicitorOrganisationPage(
      this.page,
      unregisteredOrganisationFragment,
      unregisteredOrganisationAddressFragment,
    );
  }

  get unregisteredSecondDefendantSolicitorOrganisationPage() {
    const unregisteredOrganisationFragment = new UnregisteredOrganisationFragment(
      this.page,
      partys.DEFENDANT_SOLICITOR_2,
    );
    const unregisteredOrganisationAddressFragment = new UnregisteredOrganisationAddressFragment(
      this.page,
      partys.DEFENDANT_SOLICITOR_2,
    );
    return new UnregisteredSecondDefendantSolicitorOrganisationPage(
      this.page,
      unregisteredOrganisationFragment,
      unregisteredOrganisationAddressFragment,
    );
  }

  get defendantSolicitorServiceAddressPage() {
    const serviceAddressFragment = new ServiceAddressFragment(
      this.page,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
    );
    return new DefendantSolicitorServiceAddressPage(this.page, serviceAddressFragment);
  }

  get specRespondentCorrespondenceAddressPage() {
    const correspondenceAddressFragment = new CorrespondenceAddressFragment(
      this.page,
      partys.DEFENDANT_SOLICITOR_1,
      partys.DEFENDANT_1,
    );
    return new SpecRespondentCorrespondenceAddressPage(this.page, correspondenceAddressFragment);
  }

  get defendantSolicitorEmailPage() {
    return new DefendantSolicitorEmailPage(this.page);
  }

  get defendantSolicitorEmailSpecPage() {
    return new DefendantSolicitorEmailSpecPage(this.page);
  }

  get addAnotherDefendantPage() {
    return new AddAnotherDefendantPage(this.page);
  }

  get secondDefendantPage() {
    return new SecondDefendantPage(this.page);
  }

  get secondDefendantLegalRepresentationPage() {
    return new SecondDefendantLegalRepresentationPage(this.page);
  }

  get legalRepresentationRespondent2Page() {
    return new LegalRepresentationRespondent2Page(this.page);
  }

  get sameLegalRepresentativePage() {
    return new SameLegalRepresentativePage(this.page);
  }

  get secondDefendantSolicitorOrganisationPage() {
    const organisationFragment = new OrganisationFragment(this.page, partys.DEFENDANT_2);
    return new SecondDefendantSolicitorOrganisationPage(this.page, organisationFragment);
  }

  get secondDefendantSolicitorOrganisationSpecPage() {
    const organisationRegisteredFragment = new OrganisationRegisteredFragment(
      this.page,
      partys.DEFENDANT_2,
    );
    const organisationFragment = new OrganisationFragment(this.page, partys.DEFENDANT_2);
    return new SecondDefendantSolicitorOrganisationSpecPage(
      this.page,
      organisationRegisteredFragment,
      organisationFragment,
    );
  }

  get secondDefendantSolicitorServiceAddressPage() {
    const serviceAddressFragment = new ServiceAddressFragment(
      this.page,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
    );
    return new SecondDefendantSolicitorServiceAddressPage(this.page, serviceAddressFragment);
  }

  get specRespondent2CorrespondenceAddressPage() {
    const correspondenceAddressFragment = new CorrespondenceAddressFragment(
      this.page,
      partys.DEFENDANT_SOLICITOR_2,
      partys.DEFENDANT_2,
    );
    return new SpecRespondent2CorrespondenceAddressPage(this.page, correspondenceAddressFragment);
  }

  get secondDefendantSolicitorReferencePage() {
    const defendantSolicitorReferenceFragment = new SolicitorReferenceFragment(
      this.page,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
    );
    return new SecondDefendantSolicitorReferencePage(
      this.page,
      defendantSolicitorReferenceFragment,
    );
  }

  get secondDefendantSolicitorEmailPage() {
    return new SecondDefendantSolicitorEmailPage(this.page);
  }

  get secondDefendantSolicitorEmailSpecPage() {
    return new SecondDefendantSolicitorEmailSpecPage(this.page);
  }

  get claimTypePage() {
    return new ClaimTypePage(this.page);
  }

  get flightDelayClaimPage() {
    return new FlightDelayClaimPage(this.page);
  }

  get personalInjuryType() {
    return new PersonalInjuryTypePage(this.page);
  }

  get detailsPage() {
    return new DetailsPage(this.page);
  }

  get detailsSpecPage() {
    return new DetailsSpecPage(this.page);
  }

  get uploadParticularsOfClaimPage() {
    return new UploadParticularsOfClaimPage(this.page);
  }

  get uploadCreateClaimPage() {
    const particularsOfClaimFragment = new ParticularsOfClaimFragment(this.page);
    return new UploadCreateClaimPage(this.page, particularsOfClaimFragment);
  }

  get uploadClaimDocumentPage() {
    return new UploadClaimDocumentPage(this.page);
  }

  get claimTimelineUploadPage() {
    return new ClaimTimelineUploadPage(this.page);
  }

  get claimTimelinePage() {
    const dateFragment = new DateFragment(this.page);
    return new ClaimTimelinePage(this.page, dateFragment);
  }

  get evidenceListPage() {
    return new EvidenceListPage(this.page);
  }

  get claimValuePage() {
    return new ClaimValuePage(this.page);
  }

  get claimAmountPage() {
    return new ClaimAmountPage(this.page);
  }

  get claimAmountDetailsPage() {
    return new ClaimAmountDetailsPage(this.page);
  }

  get claimInterestPage() {
    return new ClaimInterestPage(this.page);
  }

  get interestSummaryPage() {
    return new InterestSummaryPage(this.page);
  }

  get breakDownInterestPage() {
    return new BreakDownInterestPage(this.page);
  }

  get claimInterestOptionsPage() {
    return new ClaimInterestOptionsPage(this.page);
  }

  get interestClaimFromPage() {
    return new InterestClaimFromPage(this.page);
  }

  get interestClaimUntilPage() {
    return new InterestClaimUntilPage(this.page);
  }

  get interestFromSpecificDate() {
    return new InterestFromSpecificDate(this.page);
  }

  get sameRateInterestSelectionPage() {
    return new SameRateInterestSelectionPage(this.page);
  }

  get pbaNumberPage() {
    return new PbaNumberPage(this.page);
  }

  get fixedCommencementCostsPage() {
    return new FixedCommencementCostsPage(this.page);
  }

  get statementOfTruthCreateClaimPage() {
    const statementOfTruthFragment = new StatementOfTruthFragment(
      this.page,
      partys.CLAIMANT_SOLICITOR_1,
    );
    return new StatementOfTruthCreateClaimPage(this.page, statementOfTruthFragment);
  }

  get submitCreateClaimPage() {
    return new SubmitCreateClaimPage(this.page);
  }

  get confirmCreateClaimPage() {
    return new ConfirmCreateClaimPage(this.page);
  }

  get confirmCreateClaimSpecPage() {
    return new ConfirmCreateClaimSpecPage(this.page);
  }

  get confirmCreateClaimLIPPage() {
    return new ConfirmCreateClaimLIPPage(this.page);
  }
}
