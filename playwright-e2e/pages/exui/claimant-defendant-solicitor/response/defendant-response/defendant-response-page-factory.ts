import BasePageFactory from '../../../../../base/base-page-factory';
import partys from '../../../../../constants/partys';
import SingleResponse2v1Page from './common/single-response-2v1/single-response-2v1-page';
import SingleResponsePage from './common/single-response/single-response-page';
import StatmentOfTruthDefendantResponsePage from './common/statement-of-truth-defendant-response/statement-of-truth-defendant-response-page';
import SubmitDefendantResponsePage from './common/submit-defendant-response/submit-defendant-response-page';
import Confirm1v2SSDefendantResponseSpecPage from './lr-spec/confirm-defendant-response-spec/confirm-1v2SS-defendant-response-spec-page';
import ConfirmDefendantResponseSpecPage from './lr-spec/confirm-defendant-response-spec/confirm-defendant-response-spec-page';
import DefenceRoutePage from './lr-spec/defence-route/defence-route-page';
import HowToAddTimelineManualPage from './lr-spec/how-to-add-timeline-manual/how-to-add-timeline-manual-page';
import HowToAddTimelineUploadPage from './lr-spec/how-to-add-timeline-upload/how-to-add-timeline-upload-page';
import HowToAddTimelinePage from './lr-spec/how-to-add-timeline/how-to-add-timeline-page';
import MediationPage from './lr-spec/mediation/mediation-page';
import RespondentChecklistPage from './lr-spec/respondent-checklist/respondent-checklist-page';
import RespondentResponseType2v1SpecPage from './lr-spec/respondent-response-type-2v1-spec/respondent-response-type-2v1-spec-page';
import RespondentResponseType1v2SpecPage from './lr-spec/respondent-response-type-spec/respondent-response-type-1v2-spec-page';
import RespondentResponseTypeSpecPage from './lr-spec/respondent-response-type-spec/respondent-response-type-spec-page';
import ResponseConfirmDetails1v2Page from './lr-spec/response-confirm-details/response-confirm-details-1v2-page';
import ResponseConfirmDetailsPage from './lr-spec/response-confirm-details/response-confirm-details-page';
import ResponseConfirmNameAddress1v2FastPage from './lr-spec/response-confirm-name-address/response-confirm-name-address-1v2-fast-page';
import ResponseConfirmNameAddress1v2Page from './lr-spec/response-confirm-name-address/response-confirm-name-address-1v2-page';
import ResponseConfirmNameAddressPage from './lr-spec/response-confirm-name-address/response-confirm-name-address-page';
import UploadDefendantResponseSpecPage from './lr-spec/upload-defendant-response-spec/upload-defendant-response-spec-page';
import Confirm1v2DSDefendantResponsePage from './unspec/confirm-defendant-response/confirm-1v2DS-defendant-response-page';
import ConfirmDefendantResponsePage from './unspec/confirm-defendant-response/confirm-defendant-response-page';
import ConfirmDetails1v2Page from './unspec/confirm-details/confirm-details-1v2-page';
import ConfirmDetailsPage from './unspec/confirm-details/confirm-details-page';
import RespondentResponseType1v2SSPage from './unspec/respondent-response-type/respondent-response-type-1v2SS-page';
import RespondentResponseType2v1Page from './unspec/respondent-response-type/respondent-response-type-2v1-page';
import RespondentResponseTypePage from './unspec/respondent-response-type/respondent-response-type-page';
import SolicitorReferencesDefendantResponsePage from './unspec/solicitor-references-defendant-response/solicitor-references-defendant-response-page';
import UploadDefendantResponsePage from './unspec/upload-defendant-response/upload-defendant-response-page';
import ExpertPage from '../directions-questionaire/common/experts/experts-page';
import FileDirectionsQuestionnairePage from '../directions-questionaire/common/file-directions-questionnaire/file-directions-questionnaire-page';
import FixedRecoverableCostsPage from '../directions-questionaire/common/fixed-recoverable-costs/fixed-recoverable-costs-page';
import HearingSupportPage from '../directions-questionaire/common/hearing-support/hearing-support-page';
import LanguagePage from '../directions-questionaire/common/language/language-page';
import WitnessesPage from '../directions-questionaire/common/witnesses/witnesses-page';
import ApplicationPage from '../directions-questionaire/lr-spec/application/application-page';
import DisclosureOfElectronicDocumentsLRSpecPage from '../directions-questionaire/lr-spec/disclosure-of-electronic-documents-lr-spec/disclosure-of-electronic-documents-lr-spec-page';
import DisclosureOfNonElectronicDocumentsLRSpecPage from '../directions-questionaire/lr-spec/disclosure-of-non-electronic-documents-lr-spec/disclosure-of-non-electronic-documents-lr-spec-page';
import DisclosureReportPage from '../directions-questionaire/lr-spec/disclosure-report/disclosure-report-page';
import HearingLRSpecPage from '../directions-questionaire/lr-spec/hearing-lr-spec/hearing-lr-spec-page';
import RequestedCourtLRSpecPage from '../directions-questionaire/lr-spec/requested-court-lr-spec/requested-court-lr-spec-page';
import SmallClaimExpertsPage from '../directions-questionaire/lr-spec/small-claim-experts/small-claim-experts-page';
import SmallClaimHearingPage from '../directions-questionaire/lr-spec/small-claim-hearing/small-claim-hearing-page';
import SmallClaimWitnessesPage from '../directions-questionaire/lr-spec/small-claim-witnesses/small-claim-witnesses-page';
import VulnerabilityQuestionsSpecPage from '../directions-questionaire/lr-spec/vulnerability-questions-spec/vulnerability-questions-spec-page';
import WitnessesSpecPage from '../directions-questionaire/lr-spec/witnesses-spec/witnesses-spec-page';
import DisclosureOfNonElectronicDocumentsPage from '../directions-questionaire/unspec/disclosure-of-non-electronic-documents/disclosure-of-non-electronic-documents-page';
import DraftDirectionsPage from '../directions-questionaire/unspec/draft-directions/draft-directions-page';
import FurtherInformationPage from '../directions-questionaire/unspec/further-information/further-information-page';
import HearingPage from '../directions-questionaire/unspec/hearing/hearing-page';
import RequestedCourtPage from '../directions-questionaire/unspec/requested-court/requested-court-page';
import VulnerabilityQuestionsPage from '../directions-questionaire/unspec/vulnerability-questions/vulnerability-questions-page';
import MediationAvailabilityPage from '../mediation/mediation-availability/mediation-availability-page';
import MediationContactInformationPage from '../mediation/mediation-contact-information/mediation-contact-information-page';
import DateFragment from '../../../fragments/date/date-fragment';
import RemoteHearingSpecFragment from '../../../fragments/remote-hearing-spec/remote-hearing-spec-fragment';
import RemoteHearingFragment from '../../../fragments/remote-hearing/remote-hearing-fragment';
import SolicitorReferenceFragment from '../../../fragments/solicitor-reference/solicitor-reference-fragment';
import StatementOfTruthFragment from '../../../fragments/statement-of-truth/statement-of-truth-fragment';
import DateOfBirthFragment from '../../../fragments/date/date-of-birth-fragment';
import DeterminationWithoutHearingPage from '../directions-questionaire/common/determination-without-hearing/determination-without-hearing-page';
import YesOrNoFragment from '../../../fragments/yes-or-no/yes-or-no-fragment';

export default class DefendantResponsePageFactory extends BasePageFactory {
  get respondentChecklistPage() {
    return new RespondentChecklistPage(this.page);
  }

  get confirmDetailsDS1Page() {
    const dateOfBirthFragment = new DateOfBirthFragment(this.page);
    return new ConfirmDetailsPage(
      this.page,
      dateOfBirthFragment,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
    );
  }

  get confirmDetailsDS2Page() {
    const dateOfBirthFragment = new DateOfBirthFragment(this.page);
    return new ConfirmDetailsPage(
      this.page,
      dateOfBirthFragment,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
    );
  }

  get confirmDetails1v2Page() {
    const dateOfBirthFragment = new DateOfBirthFragment(this.page);
    return new ConfirmDetails1v2Page(this.page, dateOfBirthFragment);
  }

  get responseConfirmNameAddressDS1Page() {
    return new ResponseConfirmNameAddressPage(
      this.page,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
    );
  }

  get responseConfirmNameAddressDS2Page() {
    return new ResponseConfirmNameAddressPage(
      this.page,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
    );
  }

  get responseConfirmNameAddress1v2Page() {
    return new ResponseConfirmNameAddress1v2Page(this.page);
  }

  get responseConfirmNameAddress1v2FastPage() {
    return new ResponseConfirmNameAddress1v2FastPage(this.page);
  }

  get responseConfirmDetailsDS1Page() {
    const solicitorReferenceFragment = new SolicitorReferenceFragment(
      this.page,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
    );
    const dateOfBirthFragment = new DateOfBirthFragment(this.page);
    return new ResponseConfirmDetailsPage(
      this.page,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
      solicitorReferenceFragment,
      dateOfBirthFragment,
    );
  }

  get responseConfirmDetailsDS2Page() {
    const solicitorReferenceFragment = new SolicitorReferenceFragment(
      this.page,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
    );
    const dateOfBirthFragment = new DateOfBirthFragment(this.page);
    return new ResponseConfirmDetailsPage(
      this.page,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
      solicitorReferenceFragment,
      dateOfBirthFragment,
    );
  }

  get responseConfirmDetails1v2Page() {
    const solicitorReferenceFragment = new SolicitorReferenceFragment(
      this.page,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
    );
    const dateOfBirthFragment = new DateOfBirthFragment(this.page);
    return new ResponseConfirmDetails1v2Page(
      this.page,
      solicitorReferenceFragment,
      dateOfBirthFragment,
    );
  }

  get singleResponsePage() {
    return new SingleResponsePage(this.page);
  }

  get singleResponse2v1Page() {
    return new SingleResponse2v1Page(this.page);
  }

  get respondentResponseTypeDS1Page() {
    return new RespondentResponseTypePage(
      this.page,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
    );
  }

  get respondentResponseTypeDS2Page() {
    return new RespondentResponseTypePage(
      this.page,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
    );
  }

  get respondentResponseType1v2SSPage() {
    return new RespondentResponseType1v2SSPage(this.page);
  }

  get respondentResponseType2v1Page() {
    return new RespondentResponseType2v1Page(this.page);
  }

  get respondentResponseTypeSpecDS1Page() {
    return new RespondentResponseTypeSpecPage(
      this.page,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
    );
  }

  get respondentResponseTypeSpecDS2Page() {
    return new RespondentResponseTypeSpecPage(
      this.page,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
    );
  }

  get respondentResponseType1v2SpecPage() {
    return new RespondentResponseType1v2SpecPage(this.page);
  }

  get respondentResponseType2v1SpecPage() {
    return new RespondentResponseType2v1SpecPage(this.page);
  }

  get solicitorReferencesDefendantResponseDS1Page() {
    const solicitorReferenceFragment = new SolicitorReferenceFragment(
      this.page,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
    );
    return new SolicitorReferencesDefendantResponsePage(
      this.page,
      solicitorReferenceFragment,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
    );
  }

  get solicitorReferencesDefendantResponseDS2Page() {
    const solicitorReferenceFragment = new SolicitorReferenceFragment(
      this.page,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
    );
    return new SolicitorReferencesDefendantResponsePage(
      this.page,
      solicitorReferenceFragment,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
    );
  }

  get defenceRouteDS1Page() {
    const dateFragment = new DateFragment(this.page);
    return new DefenceRoutePage(
      this.page,
      dateFragment,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
    );
  }

  get defenceRouteDS2Page() {
    const dateFragment = new DateFragment(this.page);
    return new DefenceRoutePage(
      this.page,
      dateFragment,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
    );
  }

  get uploadDefendantResponseDS1Page() {
    return new UploadDefendantResponsePage(
      this.page,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
    );
  }

  get uploadDefendantResponseDS2Page() {
    return new UploadDefendantResponsePage(
      this.page,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
    );
  }

  get uploadDefendantResponseSpecDS1Page() {
    return new UploadDefendantResponseSpecPage(
      this.page,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
    );
  }

  get uploadDefendantResponseSpecDS2Page() {
    return new UploadDefendantResponseSpecPage(
      this.page,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
    );
  }

  get howToAddTimelineDS1Page() {
    return new HowToAddTimelinePage(this.page, partys.DEFENDANT_1, partys.DEFENDANT_SOLICITOR_1);
  }

  get howToAddTimelineDS2Page() {
    return new HowToAddTimelinePage(this.page, partys.DEFENDANT_2, partys.DEFENDANT_SOLICITOR_2);
  }

  get howToAddTimelineManualDS1Page() {
    const dateFragment = new DateFragment(this.page);
    return new HowToAddTimelineManualPage(
      this.page,
      dateFragment,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
    );
  }

  get howToAddTimelineManualDS2Page() {
    const dateFragment = new DateFragment(this.page);
    return new HowToAddTimelineManualPage(
      this.page,
      dateFragment,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
    );
  }

  get howToAddTimelineUploadDS1Page() {
    return new HowToAddTimelineUploadPage(
      this.page,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
    );
  }

  get howToAddTimelineUploadDS2Page() {
    return new HowToAddTimelineUploadPage(
      this.page,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
    );
  }

  get mediationDS1Page() {
    return new MediationPage(this.page, partys.DEFENDANT_1, partys.DEFENDANT_SOLICITOR_1);
  }

  get mediationDS2Page() {
    return new MediationPage(this.page, partys.DEFENDANT_2, partys.DEFENDANT_SOLICITOR_2);
  }

  get mediationContactInformationDS1Page() {
    return new MediationContactInformationPage(
      this.page,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
      partys.DEFENDANT_1_MEDIATION_FRIEND,
    );
  }

  get mediationContactInformationDS2Page() {
    return new MediationContactInformationPage(
      this.page,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
      partys.DEFENDANT_2_MEDIATION_FRIEND,
    );
  }

  get mediationAvailabilityDS1Page() {
    const dateFragment = new DateFragment(this.page);
    return new MediationAvailabilityPage(
      this.page,
      dateFragment,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
    );
  }

  get mediationAvailabilityDS2Page() {
    const dateFragment = new DateFragment(this.page);
    return new MediationAvailabilityPage(
      this.page,
      dateFragment,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
    );
  }

  get fileDirectionsQuestionaireDS1Page() {
    return new FileDirectionsQuestionnairePage(
      this.page,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
    );
  }

  get fileDirectionsQuestionaireDS2Page() {
    return new FileDirectionsQuestionnairePage(
      this.page,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
    );
  }

  get fixedRecoverableCostsDS1Page() {
    return new FixedRecoverableCostsPage(
      this.page,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
    );
  }

  get fixedRecoverableCostsDS2Page() {
    return new FixedRecoverableCostsPage(
      this.page,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
    );
  }

  get disclosureOfElectronicDocumentsLRSpecDS1Page() {
    return new DisclosureOfElectronicDocumentsLRSpecPage(
      this.page,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
    );
  }

  get disclosureOfElectronicDocumentsLRSpecDS2Page() {
    return new DisclosureOfElectronicDocumentsLRSpecPage(
      this.page,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
    );
  }

  get disclosureOfNonElectronicDocumentsLRSpecDS1Page() {
    return new DisclosureOfNonElectronicDocumentsLRSpecPage(
      this.page,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
    );
  }

  get disclosureOfNonElectronicDocumentsLRSpecDS2Page() {
    return new DisclosureOfNonElectronicDocumentsLRSpecPage(
      this.page,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
    );
  }

  get disclosureOfNonElectronicDocumentsDS1Page() {
    return new DisclosureOfNonElectronicDocumentsPage(
      this.page,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
    );
  }

  get disclosureOfNonElectronicDocumentsDS2Page() {
    return new DisclosureOfNonElectronicDocumentsPage(
      this.page,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
    );
  }

  get disclosureReportDS1Page() {
    return new DisclosureReportPage(this.page, partys.DEFENDANT_1, partys.DEFENDANT_SOLICITOR_1);
  }

  get disclosureReportDS2Page() {
    return new DisclosureReportPage(this.page, partys.DEFENDANT_2, partys.DEFENDANT_SOLICITOR_2);
  }

  get determinationWithoutHearingDS1Page() {
    const yesOrNoFragment = new YesOrNoFragment(this.page);
    return new DeterminationWithoutHearingPage(
      this.page,
      yesOrNoFragment,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
    );
  }

  get determinationWithoutHearingDS2Page() {
    const yesOrNoFragment = new YesOrNoFragment(this.page);
    return new DeterminationWithoutHearingPage(
      this.page,
      yesOrNoFragment,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
    );
  }

  get smallClaimExpertsDS1Page() {
    return new SmallClaimExpertsPage(
      this.page,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
      partys.DEFENDANT_1_EXPERT_1,
    );
  }

  get smallClaimExpertsDS2Page() {
    return new SmallClaimExpertsPage(
      this.page,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
      partys.DEFENDANT_2_EXPERT_1,
    );
  }

  get expertsDS1Page() {
    return new ExpertPage(
      this.page,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
      partys.DEFENDANT_1_EXPERT_1,
    );
  }

  get expertsDS2Page() {
    return new ExpertPage(
      this.page,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
      partys.DEFENDANT_2_EXPERT_1,
    );
  }

  get smallClaimWitnessesDS1Page() {
    return new SmallClaimWitnessesPage(
      this.page,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
      partys.DEFENDANT_1_WITNESS_1,
    );
  }

  get smallClaimWitnessesDS2Page() {
    return new SmallClaimWitnessesPage(
      this.page,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
      partys.DEFENDANT_2_WITNESS_1,
    );
  }

  get witnessesSpecDS1Page() {
    return new WitnessesSpecPage(
      this.page,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
      partys.DEFENDANT_1_WITNESS_1,
    );
  }

  get witnessesSpecDS2Page() {
    return new WitnessesSpecPage(
      this.page,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
      partys.DEFENDANT_2_WITNESS_1,
    );
  }

  get witnessesDS1Page() {
    return new WitnessesPage(
      this.page,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
      partys.DEFENDANT_1_WITNESS_1,
    );
  }

  get witnessesDS2Page() {
    return new WitnessesPage(
      this.page,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
      partys.DEFENDANT_2_WITNESS_1,
    );
  }

  get languageDS1Page() {
    return new LanguagePage(this.page, partys.DEFENDANT_1, partys.DEFENDANT_SOLICITOR_1);
  }

  get languageDS2Page() {
    return new LanguagePage(this.page, partys.DEFENDANT_2, partys.DEFENDANT_SOLICITOR_2);
  }

  get hearingDS1Page() {
    const dateFragment = new DateFragment(this.page);
    return new HearingPage(
      this.page,
      dateFragment,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
    );
  }

  get hearingDS2Page() {
    const dateFragment = new DateFragment(this.page);
    return new HearingPage(
      this.page,
      dateFragment,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
    );
  }

  get smallClaimHearingDS1Page() {
    const dateFragment = new DateFragment(this.page);
    return new SmallClaimHearingPage(
      this.page,
      dateFragment,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
    );
  }

  get smallClaimHearingDS2Page() {
    const dateFragment = new DateFragment(this.page);
    return new SmallClaimHearingPage(
      this.page,
      dateFragment,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
    );
  }

  get hearingLRSpecDS1Page() {
    const dateFragment = new DateFragment(this.page);
    return new HearingLRSpecPage(
      this.page,
      dateFragment,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
    );
  }

  get hearingLRSpecDS2Page() {
    const dateFragment = new DateFragment(this.page);
    return new HearingLRSpecPage(
      this.page,
      dateFragment,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
    );
  }

  get draftDirectionsDS1Page() {
    return new DraftDirectionsPage(this.page, partys.DEFENDANT_1, partys.DEFENDANT_SOLICITOR_1);
  }

  get draftDirectionsDS2Page() {
    return new DraftDirectionsPage(this.page, partys.DEFENDANT_2, partys.DEFENDANT_SOLICITOR_2);
  }

  get requestedCourtDS1Page() {
    const remoteHearingFragment = new RemoteHearingFragment(this.page, partys.DEFENDANT_1);
    return new RequestedCourtPage(
      this.page,
      remoteHearingFragment,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
    );
  }

  get requestedCourtDS2Page() {
    const remoteHearingFragment = new RemoteHearingFragment(this.page, partys.DEFENDANT_2);
    return new RequestedCourtPage(
      this.page,
      remoteHearingFragment,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
    );
  }

  get requestedCourtLRSpecDS1Page() {
    const remoteHearingSpecFragment = new RemoteHearingSpecFragment(this.page, partys.DEFENDANT_1);
    return new RequestedCourtLRSpecPage(
      this.page,
      remoteHearingSpecFragment,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
    );
  }

  get requestedCourtLRSpecDS2Page() {
    const remoteHearingSpecFragment = new RemoteHearingSpecFragment(this.page, partys.DEFENDANT_2);
    return new RequestedCourtLRSpecPage(
      this.page,
      remoteHearingSpecFragment,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
    );
  }

  get hearingSupportDS1Page() {
    return new HearingSupportPage(this.page, partys.DEFENDANT_1, partys.DEFENDANT_SOLICITOR_1);
  }

  get hearingSupportDS2Page() {
    return new HearingSupportPage(this.page, partys.DEFENDANT_2, partys.DEFENDANT_SOLICITOR_2);
  }

  get vulnerabilityQuestionsDS1Page() {
    return new VulnerabilityQuestionsPage(
      this.page,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
    );
  }

  get vulnerabilityQuestionsDS2Page() {
    return new VulnerabilityQuestionsPage(
      this.page,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
    );
  }

  get vulnerabilityQuestionsSpecDS1Page() {
    return new VulnerabilityQuestionsSpecPage(
      this.page,
      partys.DEFENDANT_1,
      partys.DEFENDANT_SOLICITOR_1,
    );
  }

  get vulnerabilityQuestionsSpecDS2Page() {
    return new VulnerabilityQuestionsSpecPage(
      this.page,
      partys.DEFENDANT_2,
      partys.DEFENDANT_SOLICITOR_2,
    );
  }

  get furtherInformationDS1Page() {
    return new FurtherInformationPage(this.page, partys.DEFENDANT_1, partys.DEFENDANT_SOLICITOR_1);
  }

  get furtherInformationDS2Page() {
    return new FurtherInformationPage(this.page, partys.DEFENDANT_2, partys.DEFENDANT_SOLICITOR_2);
  }

  get applicationDS1Page() {
    return new ApplicationPage(this.page, partys.DEFENDANT_1, partys.DEFENDANT_SOLICITOR_1);
  }

  get applicationDS2Page() {
    return new ApplicationPage(this.page, partys.DEFENDANT_2, partys.DEFENDANT_SOLICITOR_2);
  }

  get statementOfTruthDefendantResponseDS1Page() {
    const statementofTruthFragment = new StatementOfTruthFragment(
      this.page,
      partys.DEFENDANT_SOLICITOR_1,
    );
    return new StatmentOfTruthDefendantResponsePage(this.page, statementofTruthFragment);
  }

  get statementOfTruthDefendantResponseDS2Page() {
    const statementofTruthFragment = new StatementOfTruthFragment(
      this.page,
      partys.DEFENDANT_SOLICITOR_2,
    );
    return new StatmentOfTruthDefendantResponsePage(this.page, statementofTruthFragment);
  }

  get submitDefendantResponsePage() {
    return new SubmitDefendantResponsePage(this.page);
  }

  get confirmDefendantResponsePage() {
    return new ConfirmDefendantResponsePage(this.page);
  }

  get confirm1v2DSDefendantResponsePage() {
    return new Confirm1v2DSDefendantResponsePage(this.page);
  }

  get confirmDefendantResponseSpecPage() {
    return new ConfirmDefendantResponseSpecPage(this.page);
  }

  get confirm1v2SSDefendantResponseSpecPage() {
    return new Confirm1v2SSDefendantResponseSpecPage(this.page);
  }
}
