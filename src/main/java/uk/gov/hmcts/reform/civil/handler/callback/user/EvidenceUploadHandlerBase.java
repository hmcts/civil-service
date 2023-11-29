package uk.gov.hmcts.reform.civil.handler.callback.user;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FilenameUtils;

import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadExpert;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadTrial;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadWitness;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_APPLICANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_RESPONDENT;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;

abstract class EvidenceUploadHandlerBase extends CallbackHandler {

    private final List<CaseEvent> events;
    private final String pageId;
    private final String createShowCondition;
    protected final ObjectMapper objectMapper;
    private final Time time;
    private final CoreCaseUserService coreCaseUserService;
    private final UserService userService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;

    private static final String SPACE = " ";
    private static final String END = ".";
    private static final String DATE_FORMAT = "dd-MM-yyyy";

    protected static final String APPLICANT_DISCLOSURE = "ApplicantDisclosure";
    protected static final String APPLICANT_TWO_DISCLOSURE = "ApplicantTwoDisclosure";
    protected static final String RESPONDENT_ONE_DISCLOSURE = "RespondentOneDisclosure";
    protected static final String RESPONDENT_TWO_DISCLOSURE = "RespondentTwoDisclosure";
    protected static final String RESPONDENT_ONE_WITNESS_REFERRED = "RespondentOneWitnessReferred";
    protected static final String RESPONDENT_TWO_WITNESS_REFERRED = "RespondentTwoWitnessReferred";
    protected static final String APPLICANT_WITNESS_REFERRED = "ApplicantWitnessReferred";
    protected static final String APPLICANT_TWO_WITNESS_REFERRED = "ApplicantTwoWitnessReferred";
    protected static final String RESPONDENT_ONE_TRIAL_DOC_CORRESPONDENCE = "RespondentOneTrialDocCorrespondence";
    protected static final String RESPONDENT_TWO_TRIAL_DOC_CORRESPONDENCE = "RespondentTwoTrialDocCorrespondence";
    protected static final String APPLICANT_TRIAL_DOC_CORRESPONDENCE = "ApplicantTrialDocCorrespondence";
    protected static final String APPLICANT_TWO_TRIAL_DOC_CORRESPONDENCE = "ApplicantTwoTrialDocCorrespondence";
    protected static final String RESPONDENT_ONE_EXPERT_QUESTIONS = "RespondentOneExpertQuestions";
    protected static final String RESPONDENT_TWO_EXPERT_QUESTIONS = "RespondentTwoExpertQuestions";
    protected static final String APPLICANT_EXPERT_QUESTIONS = "ApplicantExpertQuestions";
    protected static final String APPLICANT_TWO_EXPERT_QUESTIONS = "ApplicantTwoExpertQuestions";
    protected static final String RESPONDENT_ONE_EXPERT_ANSWERS = "RespondentOneExpertAnswers";
    protected static final String RESPONDENT_TWO_EXPERT_ANSWERS = "RespondentTwoExpertAnswers";
    protected static final String APPLICANT_EXPERT_ANSWERS = "ApplicantExpertAnswers";
    protected static final String APPLICANT_TWO_EXPERT_ANSWERS = "ApplicantTwoExpertAnswers";
    protected static final String APPLICANT_EXPERT_REPORT = "ApplicantExpertReport";
    protected static final String APPLICANT_TWO_EXPERT_REPORT = "ApplicantTwoExpertReport";
    protected static final String RESPONDENT_TWO_EXPERT_REPORT = "RespondentTwoExpertReport";
    protected static final String RESPONDENT_ONE_EXPERT_REPORT = "RespondentOneExpertReport";
    protected static final String APPLICANT_EXPERT_JOINT_STATEMENT = "ApplicantExpertJointStatement";
    protected static final String APPLICANT_TWO_EXPERT_JOINT_STATEMENT = "ApplicantTwoExpertJointStatement";
    protected static final String RESPONDENT_TWO_EXPERT_JOINT_STATEMENT = "RespondentTwoExpertJointStatement";
    protected static final String RESPONDENT_ONE_EXPERT_JOINT_STATEMENT = "RespondentOneExpertJointStatement";
    protected static final String APPLICANT_WITNESS_STATEMENT = "ApplicantWitnessStatement";
    protected static final String APPLICANT_TWO_WITNESS_STATEMENT = "ApplicantTwoWitnessStatement";
    protected static final String RESPONDENT_ONE_WITNESS_STATEMENT = "RespondentOneWitnessStatement";
    protected static final String RESPONDENT_TWO_WITNESS_STATEMENT = "RespondentTwoWitnessStatement";
    protected static final String APPLICANT_WITNESS_SUMMARY = "ApplicantWitnessSummary";
    protected static final String APPLICANT_TWO_WITNESS_SUMMARY = "ApplicantTwoWitnessSummary";
    protected static final String RESPONDENT_ONE_WITNESS_SUMMARY = "RespondentOneWitnessSummary";
    protected static final String RESPONDENT_TWO_WITNESS_SUMMARY = "RespondentTwoWitnessSummary";
    protected static final String APPLICANT_WITNESS_HEARSAY = "ApplicantWitnessHearsay";
    protected static final String APPLICANT_TWO_WITNESS_HEARSAY = "ApplicantTwoWitnessHearsay";
    protected static final String RESPONDENT_ONE_WITNESS_HEARSAY = "RespondentOneWitnessHearsay";
    protected static final String RESPONDENT_TWO_WITNESS_HEARSAY = "RespondentTwoWitnessHearsay";
    protected static final String RESPONDENT_ONE_DISCLOSURE_LIST = "RespondentOneDisclosureList";
    protected static final String RESPONDENT_ONE_PRE_TRIAL_SUMMARY = "RespondentOnePreTrialSummary";
    protected static final String RESPONDENT_ONE_TRIAL_SKELETON = "RespondentOneTrialSkeleton";
    protected static final String RESPONDENT_ONE_PRECEDENT_H = "RespondentOneUploadedPrecedentH";
    protected static final String RESPONDENT_TWO_DISCLOSURE_LIST = "RespondentTwoDisclosureList";
    protected static final String RESPONDENT_TWO_PRE_TRIAL_SUMMARY = "RespondentTwoPreTrialSummary";
    protected static final String RESPONDENT_TWO_TRIAL_SKELETON = "RespondentTwoTrialSkeleton";
    protected static final String RESPONDENT_TWO_PRECEDENT_H = "RespondentTwoUploadedPrecedentH";
    protected static final String RESPONDENT_ONE_SCHEDULE_OF_COSTS = "RespondentSchedulesOfCost";
    protected static final String RESPONDENT_TWO_SCHEDULE_OF_COSTS = "RespondentTwoSchedulesOfCost";
    protected static final String APPLICANT_DISCLOSURE_LIST = "ApplicantDisclosureList";
    protected static final String APPLICANT_PRE_TRIAL_SUMMARY = "ApplicantPreTrialSummary";
    protected static final String APPLICANT_TRIAL_SKELETON = "ApplicantTrialSkeleton";
    protected static final String APPLICANT_PRECEDENT_H = "ApplicantUploadedPrecedentH";
    protected static final String APPLICANT_TWO_DISCLOSURE_LIST = "ApplicantTwoDisclosureList";
    protected static final String APPLICANT_TWO_PRE_TRIAL_SUMMARY = "ApplicantTwoPreTrialSummary";
    protected static final String APPLICANT_TWO_TRIAL_SKELETON = "ApplicantTwoTrialSkeleton";
    protected static final String APPLICANT_TWO_PRECEDENT_H = "ApplicantTwoUploadedPrecedentH";
    protected static final String APPLICANT_SCHEDULE_OF_COSTS = "ApplicantSchedulesOfCost";
    protected static final String APPLICANT_TWO_SCHEDULE_OF_COSTS = "ApplicantTwoSchedulesOfCost";
    // Notification Strings used for email
    protected static StringBuilder notificationString = new StringBuilder();
    protected static final String DISCLOSURE_LIST_TEXT = "%s - Disclosure list";
    protected static final String DISCLOSURE_TEXT = "%s - Documents for disclosure";
    protected static final String WITNESS_STATEMENT_TEXT = "%s - Witness statement";
    protected static final String WITNESS_SUMMARY_TEXT = "%s - Witness summary";
    protected static final String WITNESS_HEARSAY_TEXT = "%s - Notice of the intention to rely on hearsay evidence";
    protected static final String WITNESS_REFERRED_TEXT = "%s - Documents referred to in the statement";
    protected static final String EXPERT_REPORT_TEXT = "%s - Expert's report";
    protected static final String EXPERT_JOINT_STATEMENT_TEXT = "%s - Joint Statement of Experts / Single Joint Expert Report";
    protected static final String EXPERT_QUESTIONS_TEXT = "%s - Questions for other party's expert or joint experts";
    protected static final String EXPERT_ANSWERS_TEXT = "%s - Answer to questions asked";
    protected static final String PRE_TRIAL_SUMMARY_TEXT = "%s - Case Summary";
    protected static final String TRIAL_SKELETON_TEXT = "%s - Skeleton argument";
    protected static final String TRIAL_AUTHORITIES_TEXT = "%s - Authorities";
    protected static final String TRIAL_COSTS_TEXT = "%s - Costs";
    protected static final String TRIAL_DOC_CORRESPONDENCE_TEXT = "%s - Documentary evidence for trial";

    protected static final String OPTION_APP1 = "Claimant 1 - ";
    protected static final String OPTION_APP2 = "Claimant 2 - ";
    protected static final String OPTION_APP_BOTH = "Claimants 1 and 2";
    protected static final String OPTION_DEF1 = "Defendant 1 - ";
    protected static final String OPTION_DEF2 = "Defendant 2 - ";
    protected static final String OPTION_DEF_BOTH = "Defendant 1 and 2";
    private static final String CASE_TYPE_FLAG_NO = "do_not_show";

    private static final String SELECTED_VALUE_DEF_BOTH = "RESPONDENTBOTH";
    private static final String SELECTED_VALUE_APP_BOTH = "APPLICANTBOTH";

    protected EvidenceUploadHandlerBase(UserService userService, CoreCaseUserService coreCaseUserService,
                                        CaseDetailsConverter caseDetailsConverter,
                                        CoreCaseDataService coreCaseDataService,
                                        ObjectMapper objectMapper, Time time, List<CaseEvent> events, String pageId,
                                        String createShowCondition) {
        this.objectMapper = objectMapper;
        this.time = time;
        this.createShowCondition = createShowCondition;
        this.events = events;
        this.pageId = pageId;
        this.coreCaseUserService = coreCaseUserService;
        this.userService = userService;
        this.caseDetailsConverter = caseDetailsConverter;
        this.coreCaseDataService = coreCaseDataService;
    }

    abstract CallbackResponse validateValues(CallbackParams callbackParams, CaseData caseData);

    abstract CallbackResponse createShowCondition(CaseData caseData, UserInfo userInfo);

    abstract void applyDocumentUploadDate(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, LocalDateTime now);

    @Override
    public List<CaseEvent> handledEvents() {
        return events;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::setOptions)
            .put(callbackKey(MID, createShowCondition), this::createShow)
            .put(callbackKey(MID, pageId), this::validate)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::documentUploadTime)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    CallbackResponse setOptions(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> dynamicListOptions = new ArrayList<>();
        if (events.get(0).equals(EVIDENCE_UPLOAD_APPLICANT)) {
            if (MultiPartyScenario.isTwoVOne(caseData)) {
                dynamicListOptions.add(OPTION_APP1 + caseData.getApplicant1().getPartyName());
                dynamicListOptions.add(OPTION_APP2 + caseData.getApplicant2().getPartyName());
                dynamicListOptions.add(OPTION_APP_BOTH);
            }
        } else {
            if (MultiPartyScenario.isOneVTwoLegalRep(caseData)) {
                dynamicListOptions.add(OPTION_DEF1 + caseData.getRespondent1().getPartyName());
                dynamicListOptions.add(OPTION_DEF2 + caseData.getRespondent2().getPartyName());
                dynamicListOptions.add(OPTION_DEF_BOTH);
            }
        }
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        //determine claim path, and assign to CCD object for show hide functionality
        if (caseData.getCaseAccessCategory().equals(UNSPEC_CLAIM)) {
            caseDataBuilder.caseProgAllocatedTrack(caseData.getAllocatedTrack().name());
        } else if (caseData.getCaseAccessCategory().equals(SPEC_CLAIM)){
            caseDataBuilder.caseProgAllocatedTrack(caseData.getResponseClaimTrack());
        }
        caseDataBuilder.evidenceUploadOptions(DynamicList.fromList(dynamicListOptions));
        // was unable to null value properly in EvidenceUploadNotificationEventHandler after emails are sent,
        // so do it here if required.
        if (nonNull(caseData.getNotificationText()) && caseData.getNotificationText().equals("NULLED")) {
            caseDataBuilder.notificationText(null);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataBuilder.build().toMap(objectMapper))
                .build();
    }

    CallbackResponse createShow(CallbackParams callbackParams) {
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        return createShowCondition(callbackParams.getCaseData(), userInfo);
    }

    // CCD has limited show hide functionality, we want to show a field based on a fixed listed containing an element,
    // or a second list containing an element, AND with the addition of the user being respondent2 solicitor, the below
    // combines the list condition into one single condition, which can then be used in CCD along with the
    // caseTypeFlag condition
    CallbackResponse showCondition(CaseData caseData, UserInfo userInfo,
                                   List<EvidenceUploadWitness> witnessStatementFastTrack,
                                   List<EvidenceUploadWitness> witnessStatementSmallTrack,
                                   List<EvidenceUploadWitness> witnessSummaryFastTrack,
                                   List<EvidenceUploadWitness> witnessSummarySmallTrack,
                                   List<EvidenceUploadWitness> witnessReferredFastTrack,
                                   List<EvidenceUploadWitness> witnessReferredSmallTrack,
                                   List<EvidenceUploadExpert> expertReportFastTrack,
                                   List<EvidenceUploadExpert> expertReportSmallTrack,
                                   List<EvidenceUploadExpert> expertJointFastTrack,
                                   List<EvidenceUploadExpert> expertJointSmallTrack,
                                   List<EvidenceUploadTrial> trialAuthorityFastTrack,
                                   List<EvidenceUploadTrial> trialAuthoritySmallTrack,
                                   List<EvidenceUploadTrial> trialCostsFastTrack,
                                   List<EvidenceUploadTrial> trialCostsSmallTrack,
                                   List<EvidenceUploadTrial> trialDocumentaryFastTrack,
                                   List<EvidenceUploadTrial> trialDocumentarySmallTrack
                                   ) {

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        //For case which are 1v1, 2v1  we show respondent fields for documents to be uploaded,
        //if a case is 1v2 and different solicitors we want to show separate fields for each respondent solicitor i.e.
        //RESPONDENTSOLICITORTWO and RESPONDENTSOLICITORONE
        //if a case is 1v2 with same solicitor they will see respondent 2 fields as they have RESPONDENTSOLICITORTWO role
        //default flag for respondent 1 solicitor
        caseDataBuilder.caseTypeFlag(CASE_TYPE_FLAG_NO);

        boolean multiParts = Objects.nonNull(caseData.getEvidenceUploadOptions())
                && !caseData.getEvidenceUploadOptions().getListItems().isEmpty();
        if (events.get(0).equals(EVIDENCE_UPLOAD_APPLICANT)) {
            //2v1, app2 selected
            if (multiParts
                    && caseData.getEvidenceUploadOptions()
                    .getValue().getLabel().startsWith(OPTION_APP2)) {
                caseDataBuilder.caseTypeFlag("ApplicantTwoFields");
            }
        } else if (events.get(0).equals(EVIDENCE_UPLOAD_RESPONDENT)) {
            //1v2 same sol, def2 selected
            if ((multiParts
                    && caseData.getEvidenceUploadOptions()
                    .getValue().getLabel().startsWith(OPTION_DEF2))
                    //1v2 dif sol, log in as def2
                    || (!multiParts && Objects.nonNull(caseData.getCcdCaseReference())
                        && coreCaseUserService.userHasCaseRole(caseData.getCcdCaseReference()
                        .toString(), userInfo.getUid(), RESPONDENTSOLICITORTWO))) {
                caseDataBuilder.caseTypeFlag("RespondentTwoFields");
            }
        }

        // clears the flag, as otherwise if the user returns to previous screen and unselects an option,
        // which was previously selected, the option will still be shown
        caseDataBuilder.witnessStatementFlag(CASE_TYPE_FLAG_NO);
        caseDataBuilder.witnessSummaryFlag(CASE_TYPE_FLAG_NO);
        caseDataBuilder.witnessReferredStatementFlag(CASE_TYPE_FLAG_NO);
        caseDataBuilder.expertReportFlag(CASE_TYPE_FLAG_NO);
        caseDataBuilder.expertJointFlag(CASE_TYPE_FLAG_NO);
        caseDataBuilder.trialAuthorityFlag(CASE_TYPE_FLAG_NO);
        caseDataBuilder.trialCostsFlag(CASE_TYPE_FLAG_NO);
        caseDataBuilder.trialDocumentaryFlag(CASE_TYPE_FLAG_NO);

        // Based on claim type being fast track or small claims, there will be two different lists to select from
        // for either list we then want to display a (same) document upload field corresponding,
        // below combines what would have been two separate show conditions in CCD, into a single flag
        if (nonNull(witnessStatementFastTrack) && witnessStatementFastTrack.contains(EvidenceUploadWitness.WITNESS_STATEMENT)
            || nonNull(witnessStatementSmallTrack) && witnessStatementSmallTrack.contains(EvidenceUploadWitness.WITNESS_STATEMENT)) {
            caseDataBuilder.witnessStatementFlag("show_witness_statement");
        }
        if (nonNull(witnessSummaryFastTrack) && witnessSummaryFastTrack.contains(EvidenceUploadWitness.WITNESS_SUMMARY)
            || nonNull(witnessSummarySmallTrack) && witnessSummarySmallTrack.contains(EvidenceUploadWitness.WITNESS_SUMMARY)) {
            caseDataBuilder.witnessSummaryFlag("show_witness_summary");
        }
        if (nonNull(witnessReferredFastTrack) && witnessReferredFastTrack.contains(EvidenceUploadWitness.DOCUMENTS_REFERRED)
            || nonNull(witnessReferredSmallTrack) && witnessReferredSmallTrack.contains(EvidenceUploadWitness.DOCUMENTS_REFERRED)) {
            caseDataBuilder.witnessReferredStatementFlag("show_witness_referred");
        }
        if (nonNull(expertReportFastTrack) && expertReportFastTrack.contains(EvidenceUploadExpert.EXPERT_REPORT)
            || nonNull(expertReportSmallTrack) && expertReportSmallTrack.contains(EvidenceUploadExpert.EXPERT_REPORT)) {
            caseDataBuilder.expertReportFlag("show_expert_report");
        }
        if (nonNull(expertJointFastTrack) && expertJointFastTrack.contains(EvidenceUploadExpert.JOINT_STATEMENT)
            || nonNull(expertJointSmallTrack) && expertJointSmallTrack.contains(EvidenceUploadExpert.JOINT_STATEMENT)) {
            caseDataBuilder.expertJointFlag("show_joint_expert");
        }
        if (nonNull(trialAuthorityFastTrack) && trialAuthorityFastTrack.contains(EvidenceUploadTrial.AUTHORITIES)
            || nonNull(trialAuthoritySmallTrack) && trialAuthoritySmallTrack.contains(EvidenceUploadTrial.AUTHORITIES)) {
            caseDataBuilder.trialAuthorityFlag("show_trial_authority");
        }
        if (nonNull(trialCostsFastTrack) && trialCostsFastTrack.contains(EvidenceUploadTrial.COSTS)
            || nonNull(trialCostsSmallTrack) && trialCostsSmallTrack.contains(EvidenceUploadTrial.COSTS)) {
            caseDataBuilder.trialCostsFlag("show_trial_costs");
        }
        if (nonNull(trialDocumentaryFastTrack) && trialDocumentaryFastTrack.contains(EvidenceUploadTrial.DOCUMENTARY)
            || nonNull(trialDocumentarySmallTrack) && trialDocumentarySmallTrack.contains(EvidenceUploadTrial.DOCUMENTARY)) {
            caseDataBuilder.trialDocumentaryFlag("show_trial_documentary");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    CallbackResponse validate(CallbackParams callbackParams) {
        return validateValues(callbackParams, callbackParams.getCaseData());
    }

    CallbackResponse validateValuesParty(List<Element<UploadEvidenceDocumentType>> uploadEvidenceDocumentType,
                                         List<Element<UploadEvidenceWitness>> uploadEvidenceWitness1,
                                         List<Element<UploadEvidenceWitness>> uploadEvidenceWitness2,
                                         List<Element<UploadEvidenceWitness>> uploadEvidenceWitness3,
                                         List<Element<UploadEvidenceDocumentType>> witnessDocumentReferred,
                                         List<Element<UploadEvidenceExpert>> uploadEvidenceExpert1,
                                         List<Element<UploadEvidenceExpert>> uploadEvidenceExpert2,
                                         List<Element<UploadEvidenceExpert>> uploadEvidenceExpert3,
                                         List<Element<UploadEvidenceExpert>> uploadEvidenceExpert4,
                                         List<Element<UploadEvidenceDocumentType>> trialDocumentEvidence) {
        List<String> errors = new ArrayList<>();

        checkDateCorrectness(time, errors, uploadEvidenceDocumentType, date -> date.getValue()
                                 .getDocumentIssuedDate(),
                             "Invalid date: \"Documents for disclosure\" "
                                 + "date entered must not be in the future (1).");

        checkDateCorrectness(time, errors, uploadEvidenceWitness1, date -> date.getValue()
                                 .getWitnessOptionUploadDate(),
                             "Invalid date: \"witness statement\" "
                                 + "date entered must not be in the future (2).");

        checkDateCorrectness(time, errors, uploadEvidenceWitness2, date -> date.getValue()
                                 .getWitnessOptionUploadDate(),
                             "Invalid date: \"witness summary\" "
                                 + "date entered must not be in the future (3).");

        checkDateCorrectness(time, errors, uploadEvidenceWitness3, date -> date.getValue()
                                 .getWitnessOptionUploadDate(),
                             "Invalid date: \"Notice of the intention to rely on hearsay evidence\" "
                                 + "date entered must not be in the future (4).");

        checkDateCorrectness(time, errors, witnessDocumentReferred, date -> date.getValue()
                                 .getDocumentIssuedDate(),
                             "Invalid date: \"Documents referred to in the statement\" "
                                 + "date entered must not be in the future (5).");

        checkDateCorrectness(time, errors, uploadEvidenceExpert1, date -> date.getValue()
                                 .getExpertOptionUploadDate(),
                             "Invalid date: \"Expert's report\""
                                 + " date entered must not be in the future (6).");
        checkDateCorrectness(time, errors, uploadEvidenceExpert2, date -> date.getValue()
                                 .getExpertOptionUploadDate(),
                             "Invalid date: \"Joint statement of experts\" "
                                 + "date entered must not be in the future (7).");
        checkDateCorrectness(time, errors, uploadEvidenceExpert3, date -> date.getValue()
                                 .getExpertOptionUploadDate(),
                             "Invalid date: \"Questions for other party's expert or joint experts\" "
                                 + "expert statement date entered must not be in the future (8).");
        checkDateCorrectness(time, errors, uploadEvidenceExpert4, date -> date.getValue()
                                 .getExpertOptionUploadDate(),
                             "Invalid date: \"Answers to questions asked by the other party\" "
                                 + "date entered must not be in the future (9).");

        checkDateCorrectness(time, errors, trialDocumentEvidence, date -> date.getValue()
                                 .getDocumentIssuedDate(),
                             "Invalid date: \"Documentary evidence for trial\" "
                                 + "date entered must not be in the future (10).");

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    <T> void checkDateCorrectness(Time time, List<String> errors, List<Element<T>> documentUpload,
                                  Function<Element<T>, LocalDate> dateExtractor, String errorMessage) {
        if (documentUpload == null) {
            return;
        }
        documentUpload.forEach(date -> {
            LocalDate dateToCheck = dateExtractor.apply(date);
            if (dateToCheck.isAfter(time.now().toLocalDate())) {
                errors.add(errorMessage);
            }
        });
    }

    public <T> void setCategoryIdAndRenameDoc(List<Element<T>> documentUpload, Function<Element<T>,
        Document> documentExtractor, String theID, String docNotificationText, Function<Element<T>,
        LocalDateTime> documentDateTimeExtractor, String claimantDefendantString) {
        if (documentUpload == null || documentUpload.isEmpty()) {
            return;
        }
        LocalDateTime midnight = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
        renameDocuments(documentUpload, theID);
        documentUpload.forEach(document -> {
            Document documentToAddId = documentExtractor.apply(document);
            documentToAddId.setCategoryID(theID);
            LocalDateTime dateTime = documentDateTimeExtractor.apply(document);
            if (dateTime.isAfter(midnight)) {
                String updateNotificationText = format(docNotificationText, claimantDefendantString);
                if (!notificationString.toString().contains(updateNotificationText)) {
                    notificationString.append("\n").append(updateNotificationText);
                }
            }
        });
    }

    private <T> void renameDocuments(List<Element<T>> documentUpload, String theId) {
        String prefix = null;
        switch (theId) {
            case APPLICANT_DISCLOSURE,
                    APPLICANT_TWO_DISCLOSURE,
                    RESPONDENT_ONE_DISCLOSURE,
                    RESPONDENT_TWO_DISCLOSURE:
                prefix = "Document for disclosure";
                renameUploadEvidenceDocumentType(documentUpload, prefix);
                break;
            case RESPONDENT_ONE_WITNESS_REFERRED,
                    RESPONDENT_TWO_WITNESS_REFERRED,
                    APPLICANT_WITNESS_REFERRED,
                    APPLICANT_TWO_WITNESS_REFERRED:
                prefix = " referred to in the statement of ";
                renameUploadEvidenceDocumentTypeWithName(documentUpload, prefix);
                break;
            case RESPONDENT_ONE_TRIAL_DOC_CORRESPONDENCE,
                    RESPONDENT_TWO_TRIAL_DOC_CORRESPONDENCE,
                    APPLICANT_TRIAL_DOC_CORRESPONDENCE,
                    APPLICANT_TWO_TRIAL_DOC_CORRESPONDENCE:
                prefix = "Documentary Evidence";
                renameUploadEvidenceDocumentType(documentUpload, prefix);
                break;
            case RESPONDENT_ONE_EXPERT_QUESTIONS,
                    RESPONDENT_TWO_EXPERT_QUESTIONS,
                    APPLICANT_EXPERT_QUESTIONS,
                    APPLICANT_TWO_EXPERT_QUESTIONS:
                renameUploadEvidenceExpert(documentUpload, true);
                break;
            case RESPONDENT_ONE_EXPERT_ANSWERS,
                    RESPONDENT_TWO_EXPERT_ANSWERS,
                    APPLICANT_EXPERT_ANSWERS,
                    APPLICANT_TWO_EXPERT_ANSWERS:
                renameUploadEvidenceExpert(documentUpload, false);
                break;
            case APPLICANT_EXPERT_REPORT,
                    APPLICANT_TWO_EXPERT_REPORT,
                    RESPONDENT_TWO_EXPERT_REPORT,
                    RESPONDENT_ONE_EXPERT_REPORT:
                prefix = "Experts report";
                renameUploadReportExpert(documentUpload, prefix, true);
                break;
            case APPLICANT_EXPERT_JOINT_STATEMENT,
                    APPLICANT_TWO_EXPERT_JOINT_STATEMENT,
                    RESPONDENT_TWO_EXPERT_JOINT_STATEMENT,
                    RESPONDENT_ONE_EXPERT_JOINT_STATEMENT:
                prefix = "Joint report";
                renameUploadReportExpert(documentUpload, prefix, false);
                break;
            case APPLICANT_WITNESS_STATEMENT,
                    APPLICANT_TWO_WITNESS_STATEMENT,
                    RESPONDENT_ONE_WITNESS_STATEMENT,
                    RESPONDENT_TWO_WITNESS_STATEMENT:
                prefix = "Witness Statement of";
                renameUploadEvidenceWitness(documentUpload, prefix, true);
                break;
            case APPLICANT_WITNESS_SUMMARY,
                    APPLICANT_TWO_WITNESS_SUMMARY,
                    RESPONDENT_ONE_WITNESS_SUMMARY,
                    RESPONDENT_TWO_WITNESS_SUMMARY:
                prefix = "Witness Summary of";
                renameUploadEvidenceWitness(documentUpload, prefix, true);
                break;
            case APPLICANT_WITNESS_HEARSAY,
                    APPLICANT_TWO_WITNESS_HEARSAY,
                    RESPONDENT_ONE_WITNESS_HEARSAY,
                    RESPONDENT_TWO_WITNESS_HEARSAY:
                prefix = "Hearsay evidence";
                renameUploadEvidenceWitness(documentUpload, prefix, true);
                break;
            default:
                break;
        }
    }

    private <T> void renameUploadEvidenceWitness(final List<Element<T>> documentUpload,
                                              String prefix, boolean date) {
        documentUpload.forEach(x -> {
            UploadEvidenceWitness type = (UploadEvidenceWitness) x.getValue();
            String ext = FilenameUtils.getExtension(type.getWitnessOptionDocument().getDocumentFileName());
            String newName = prefix
                    + SPACE
                    + type.getWitnessOptionName()
                    + (date ? SPACE + type.getWitnessOptionUploadDate()
                        .format(DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.UK)) : "")
                    + END + ext;
            type.getWitnessOptionDocument().setDocumentFileName(newName);
        });
    }

    private <T> void renameUploadReportExpert(final List<Element<T>> documentUpload,
                                              String prefix, boolean single) {
        documentUpload.forEach(x -> {
            UploadEvidenceExpert type = (UploadEvidenceExpert) x.getValue();
            String ext = FilenameUtils.getExtension(type.getExpertDocument().getDocumentFileName());
            String newName = prefix
                    + SPACE
                    + type.getExpertOptionName()
                    + SPACE
                    + (single ? type.getExpertOptionExpertise() : type.getExpertOptionExpertises())
                    + SPACE
                    + type.getExpertOptionUploadDate()
                        .format(DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.UK))
                    + END + ext;
            type.getExpertDocument().setDocumentFileName(newName);
        });
    }

    private <T> void renameUploadEvidenceExpert(final List<Element<T>> documentUpload, boolean question) {
        documentUpload.forEach(x -> {
            UploadEvidenceExpert type = (UploadEvidenceExpert) x.getValue();
            String ext = FilenameUtils.getExtension(type.getExpertDocument().getDocumentFileName());
            String newName = type.getExpertOptionName()
                    + SPACE
                    + type.getExpertOptionOtherParty()
                    + SPACE
                    + (question ? type.getExpertDocumentQuestion() : type.getExpertDocumentAnswer())
                    + END + ext;
            type.getExpertDocument().setDocumentFileName(newName);
        });
    }

    private <T> void renameUploadEvidenceDocumentTypeWithName(final List<Element<T>> documentUpload, String body) {
        documentUpload.forEach(x -> {
            UploadEvidenceDocumentType type = (UploadEvidenceDocumentType) x.getValue();
            String ext = FilenameUtils.getExtension(type.getDocumentUpload().getDocumentFileName());
            String newName = type.getTypeOfDocument()
                    + body
                    + type.getWitnessOptionName()
                    + SPACE
                    + type.getDocumentIssuedDate()
                    .format(DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.UK))
                    + END + ext;
            type.getDocumentUpload().setDocumentFileName(newName);
        });
    }

    private <T> void renameUploadEvidenceDocumentType(final List<Element<T>> documentUpload, String prefix) {
        documentUpload.forEach(x -> {
            UploadEvidenceDocumentType type = (UploadEvidenceDocumentType) x.getValue();
            String ext = FilenameUtils.getExtension(type.getDocumentUpload().getDocumentFileName());
            String newName = prefix
                    + SPACE
                    + type.getTypeOfDocument()
                    + SPACE
                    + type.getDocumentIssuedDate()
                        .format(DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.UK))
                    + END + ext;
            type.getDocumentUpload().setDocumentFileName(newName);
        });
    }

    static void getNotificationText(CaseData caseData) {
        notificationString = new StringBuilder();
        if (caseData.getNotificationText() != null) {
            notificationString = new StringBuilder(caseData.getNotificationText());
        }
    }

    abstract void updateDocumentListUploadedAfterBundle(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, CaseData caseData);

    private void updateDocumentListUploadedAfterBundle(CaseData caseData, CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        if (nonNull(caseData.getCaseBundles()) && !caseData.getCaseBundles().isEmpty()) {
            updateDocumentListUploadedAfterBundle(caseDataBuilder, caseData);
        }
    }

    CallbackResponse documentUploadTime(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        // If notification has already been populated in current day, we want to append to that existing notification
        getNotificationText(caseData);
        applyDocumentUploadDate(caseDataBuilder, time.now());
        updateDocumentListUploadedAfterBundle(caseData, caseDataBuilder);

        String selectedRole = getSelectedRole(callbackParams);
        if (selectedRole.equals(RESPONDENTSOLICITORONE.name()) || selectedRole.equals(SELECTED_VALUE_DEF_BOTH)) {
            String defendantString = "Defendant 1";
            if (selectedRole.equals(SELECTED_VALUE_DEF_BOTH)) {
                defendantString = "Both defendants";
            }
            setCategoryIdAndRenameDoc(caseData.getDocumentDisclosureListRes(), document -> document.getValue().getDocumentUpload(), RESPONDENT_ONE_DISCLOSURE_LIST,
                                      DISCLOSURE_LIST_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), defendantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentForDisclosureRes(), document -> document.getValue().getDocumentUpload(), RESPONDENT_ONE_DISCLOSURE,
                                      DISCLOSURE_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), defendantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentWitnessStatementRes(), document -> document.getValue().getWitnessOptionDocument(), RESPONDENT_ONE_WITNESS_STATEMENT,
                                      WITNESS_STATEMENT_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), defendantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentWitnessSummaryRes(), document -> document.getValue().getWitnessOptionDocument(), RESPONDENT_ONE_WITNESS_SUMMARY,
                                      WITNESS_SUMMARY_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), defendantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentHearsayNoticeRes(), document -> document.getValue().getWitnessOptionDocument(), RESPONDENT_ONE_WITNESS_HEARSAY,
                                      WITNESS_HEARSAY_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), defendantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentReferredInStatementRes(), document -> document.getValue().getDocumentUpload(), RESPONDENT_ONE_WITNESS_REFERRED,
                                      WITNESS_REFERRED_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), defendantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentExpertReportRes(), document -> document.getValue().getExpertDocument(), RESPONDENT_ONE_EXPERT_REPORT,
                                      EXPERT_REPORT_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), defendantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentJointStatementRes(), document -> document.getValue().getExpertDocument(), RESPONDENT_ONE_EXPERT_JOINT_STATEMENT,
                                      EXPERT_JOINT_STATEMENT_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), defendantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentQuestionsRes(), document -> document.getValue().getExpertDocument(), RESPONDENT_ONE_EXPERT_QUESTIONS,
                                      EXPERT_QUESTIONS_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), defendantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentAnswersRes(), document -> document.getValue().getExpertDocument(), RESPONDENT_ONE_EXPERT_ANSWERS,
                                      EXPERT_ANSWERS_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), defendantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentCaseSummaryRes(), document -> document.getValue().getDocumentUpload(), RESPONDENT_ONE_PRE_TRIAL_SUMMARY,
                                      PRE_TRIAL_SUMMARY_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), defendantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentSkeletonArgumentRes(), document -> document.getValue().getDocumentUpload(), RESPONDENT_ONE_TRIAL_SKELETON,
                                      TRIAL_SKELETON_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), defendantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentAuthoritiesRes(), document -> document.getValue().getDocumentUpload(), RESPONDENT_ONE_PRECEDENT_H,
                                      TRIAL_AUTHORITIES_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), defendantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentCostsRes(), document -> document.getValue().getDocumentUpload(), RESPONDENT_ONE_SCHEDULE_OF_COSTS,
                                      TRIAL_COSTS_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), defendantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentEvidenceForTrialRes(), document -> document.getValue().getDocumentUpload(), RESPONDENT_ONE_TRIAL_DOC_CORRESPONDENCE,
                                      TRIAL_DOC_CORRESPONDENCE_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), defendantString);
            if (selectedRole.equals(SELECTED_VALUE_DEF_BOTH)) {
                caseData = copyResp1ChangesToResp2(caseData, caseDataBuilder);
            }
        }
        if (selectedRole.equals(RESPONDENTSOLICITORTWO.name())) {
            String defendantString =  "Defendant 2";
            setCategoryIdAndRenameDoc(caseData.getDocumentDisclosureListRes2(), document -> document.getValue().getDocumentUpload(), RESPONDENT_TWO_DISCLOSURE_LIST,
                                      DISCLOSURE_LIST_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), defendantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentForDisclosureRes2(), document -> document.getValue().getDocumentUpload(), RESPONDENT_TWO_DISCLOSURE,
                                      DISCLOSURE_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), defendantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentWitnessStatementRes2(), document -> document.getValue().getWitnessOptionDocument(), RESPONDENT_TWO_WITNESS_STATEMENT,
                                      WITNESS_STATEMENT_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), defendantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentWitnessSummaryRes2(), document -> document.getValue().getWitnessOptionDocument(), RESPONDENT_TWO_WITNESS_SUMMARY,
                                      WITNESS_SUMMARY_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), defendantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentHearsayNoticeRes2(), document -> document.getValue().getWitnessOptionDocument(), RESPONDENT_TWO_WITNESS_HEARSAY,
                                      WITNESS_HEARSAY_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), defendantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentReferredInStatementRes2(), document -> document.getValue().getDocumentUpload(), RESPONDENT_TWO_WITNESS_REFERRED,
                                      WITNESS_REFERRED_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), defendantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentExpertReportRes2(), document -> document.getValue().getExpertDocument(), RESPONDENT_TWO_EXPERT_REPORT,
                                      EXPERT_REPORT_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), defendantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentJointStatementRes2(), document -> document.getValue().getExpertDocument(), RESPONDENT_TWO_EXPERT_JOINT_STATEMENT,
                                      EXPERT_JOINT_STATEMENT_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), defendantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentQuestionsRes2(), document -> document.getValue().getExpertDocument(), RESPONDENT_TWO_EXPERT_QUESTIONS,
                                      EXPERT_QUESTIONS_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), defendantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentAnswersRes2(), document -> document.getValue().getExpertDocument(), RESPONDENT_TWO_EXPERT_ANSWERS,
                                      EXPERT_ANSWERS_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), defendantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentCaseSummaryRes2(), document -> document.getValue().getDocumentUpload(), RESPONDENT_TWO_PRE_TRIAL_SUMMARY,
                                      PRE_TRIAL_SUMMARY_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), defendantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentSkeletonArgumentRes2(), document -> document.getValue().getDocumentUpload(), RESPONDENT_TWO_TRIAL_SKELETON,
                                      TRIAL_SKELETON_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), defendantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentAuthoritiesRes2(), document -> document.getValue().getDocumentUpload(), RESPONDENT_TWO_PRECEDENT_H,
                                      TRIAL_AUTHORITIES_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), defendantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentCostsRes2(), document -> document.getValue().getDocumentUpload(), RESPONDENT_TWO_SCHEDULE_OF_COSTS,
                                      TRIAL_COSTS_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), defendantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentEvidenceForTrialRes2(), document -> document.getValue().getDocumentUpload(), RESPONDENT_TWO_TRIAL_DOC_CORRESPONDENCE,
                                      TRIAL_DOC_CORRESPONDENCE_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), defendantString);
        }

        if (selectedRole.equals(CaseRole.APPLICANTSOLICITORONE.name()) || selectedRole.equals(SELECTED_VALUE_APP_BOTH)) {
            String claimantString = "Claimant 1";
            if (selectedRole.equals(SELECTED_VALUE_APP_BOTH)) {
                claimantString = "Both claimants";
            }
            setCategoryIdAndRenameDoc(caseData.getDocumentDisclosureList(), document -> document.getValue().getDocumentUpload(),  APPLICANT_DISCLOSURE_LIST,
                                      DISCLOSURE_LIST_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), claimantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentForDisclosure(), document -> document.getValue().getDocumentUpload(), APPLICANT_DISCLOSURE,
                                      DISCLOSURE_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), claimantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentWitnessStatement(), document -> document.getValue().getWitnessOptionDocument(), APPLICANT_WITNESS_STATEMENT,
                                      WITNESS_STATEMENT_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), claimantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentWitnessSummary(), document -> document.getValue().getWitnessOptionDocument(), APPLICANT_WITNESS_SUMMARY,
                                      WITNESS_SUMMARY_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), claimantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentHearsayNotice(), document -> document.getValue().getWitnessOptionDocument(), APPLICANT_WITNESS_HEARSAY,
                                      WITNESS_HEARSAY_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), claimantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentReferredInStatement(), document -> document.getValue().getDocumentUpload(), APPLICANT_WITNESS_REFERRED,
                                      WITNESS_REFERRED_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), claimantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentExpertReport(), document -> document.getValue().getExpertDocument(), APPLICANT_EXPERT_REPORT,
                                      EXPERT_REPORT_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), claimantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentJointStatement(), document -> document.getValue().getExpertDocument(), APPLICANT_EXPERT_JOINT_STATEMENT,
                                      EXPERT_JOINT_STATEMENT_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), claimantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentQuestions(), document -> document.getValue().getExpertDocument(), APPLICANT_EXPERT_QUESTIONS,
                                      EXPERT_QUESTIONS_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), claimantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentAnswers(), document -> document.getValue().getExpertDocument(), APPLICANT_EXPERT_ANSWERS,
                                      EXPERT_ANSWERS_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), claimantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentCaseSummary(), document -> document.getValue().getDocumentUpload(), APPLICANT_PRE_TRIAL_SUMMARY,
                                      PRE_TRIAL_SUMMARY_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), claimantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentSkeletonArgument(), document -> document.getValue().getDocumentUpload(), APPLICANT_TRIAL_SKELETON,
                                      TRIAL_SKELETON_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), claimantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentAuthorities(), document -> document.getValue().getDocumentUpload(), APPLICANT_PRECEDENT_H,
                                      TRIAL_AUTHORITIES_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), claimantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentCosts(), document -> document.getValue().getDocumentUpload(), APPLICANT_SCHEDULE_OF_COSTS,
                                      TRIAL_COSTS_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), claimantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentEvidenceForTrial(), document -> document.getValue().getDocumentUpload(), APPLICANT_TRIAL_DOC_CORRESPONDENCE,
                                      TRIAL_DOC_CORRESPONDENCE_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), claimantString);
            if (selectedRole.equals(SELECTED_VALUE_APP_BOTH)) {
                caseData = copyApp1ChangesToApp2(caseData, caseDataBuilder);
            }
        }

        if (selectedRole.equals("APPLICANTSOLICITORTWO")) {
            String claimantString =  "Claimant 2";
            setCategoryIdAndRenameDoc(caseData.getDocumentDisclosureListApp2(), document -> document.getValue().getDocumentUpload(), APPLICANT_TWO_DISCLOSURE_LIST,
                                      DISCLOSURE_LIST_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), claimantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentForDisclosureApp2(), document -> document.getValue().getDocumentUpload(), APPLICANT_TWO_DISCLOSURE,
                                      DISCLOSURE_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), claimantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentWitnessStatementApp2(), document -> document.getValue().getWitnessOptionDocument(), APPLICANT_TWO_WITNESS_STATEMENT,
                                      WITNESS_STATEMENT_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), claimantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentWitnessSummaryApp2(), document -> document.getValue().getWitnessOptionDocument(), APPLICANT_TWO_WITNESS_SUMMARY,
                                      WITNESS_SUMMARY_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), claimantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentHearsayNoticeApp2(), document -> document.getValue().getWitnessOptionDocument(), APPLICANT_TWO_WITNESS_HEARSAY,
                                      WITNESS_HEARSAY_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), claimantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentReferredInStatementApp2(), document -> document.getValue().getDocumentUpload(), APPLICANT_TWO_WITNESS_REFERRED,
                                      WITNESS_REFERRED_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), claimantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentExpertReportApp2(), document -> document.getValue().getExpertDocument(), APPLICANT_TWO_EXPERT_REPORT,
                                      EXPERT_REPORT_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), claimantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentJointStatementApp2(), document -> document.getValue().getExpertDocument(), APPLICANT_TWO_EXPERT_JOINT_STATEMENT,
                                      EXPERT_JOINT_STATEMENT_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), claimantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentQuestionsApp2(), document -> document.getValue().getExpertDocument(), APPLICANT_TWO_EXPERT_QUESTIONS,
                                      EXPERT_QUESTIONS_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), claimantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentAnswersApp2(), document -> document.getValue().getExpertDocument(), APPLICANT_TWO_EXPERT_ANSWERS,
                                      EXPERT_ANSWERS_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), claimantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentCaseSummaryApp2(), document -> document.getValue().getDocumentUpload(), APPLICANT_TWO_PRE_TRIAL_SUMMARY,
                                      PRE_TRIAL_SUMMARY_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), claimantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentSkeletonArgumentApp2(), document -> document.getValue().getDocumentUpload(), APPLICANT_TWO_TRIAL_SKELETON,
                                      TRIAL_SKELETON_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), claimantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentAuthoritiesApp2(), document -> document.getValue().getDocumentUpload(), APPLICANT_TWO_PRECEDENT_H,
                                      TRIAL_AUTHORITIES_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), claimantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentCostsApp2(), document -> document.getValue().getDocumentUpload(), APPLICANT_TWO_SCHEDULE_OF_COSTS,
                                      TRIAL_COSTS_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), claimantString);
            setCategoryIdAndRenameDoc(caseData.getDocumentEvidenceForTrialApp2(), document -> document.getValue().getDocumentUpload(), APPLICANT_TWO_TRIAL_DOC_CORRESPONDENCE,
                                      TRIAL_DOC_CORRESPONDENCE_TEXT, documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), claimantString);
        }

        // null the values of the lists, so that on future retriggers of the event, they are blank
        caseDataBuilder.disclosureSelectionEvidence(null);
        caseDataBuilder.disclosureSelectionEvidenceRes(null);
        caseDataBuilder.witnessSelectionEvidence(null);
        caseDataBuilder.witnessSelectionEvidenceSmallClaim(null);
        caseDataBuilder.witnessSelectionEvidenceRes(null);
        caseDataBuilder.witnessSelectionEvidenceSmallClaimRes(null);
        caseDataBuilder.expertSelectionEvidenceRes(null);
        caseDataBuilder.expertSelectionEvidence(null);
        caseDataBuilder.expertSelectionEvidenceSmallClaim(null);
        caseDataBuilder.expertSelectionEvidenceSmallClaimRes(null);
        caseDataBuilder.trialSelectionEvidence(null);
        caseDataBuilder.trialSelectionEvidenceSmallClaim(null);
        caseDataBuilder.trialSelectionEvidenceRes(null);
        caseDataBuilder.trialSelectionEvidenceSmallClaimRes(null);
        caseDataBuilder.notificationText(notificationString.toString());
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CaseData copyResp1ChangesToResp2(CaseData caseData, CaseData.CaseDataBuilder<?, ?> builder) {
        CaseData caseDataBefore = caseDetailsConverter
                .toCaseData(coreCaseDataService.getCase(caseData.getCcdCaseReference()));
        List<Element<UploadEvidenceDocumentType>> evidenceDocToCopy =
                compareAndCopy(caseDataBefore.getDocumentDisclosureListRes(),
                    caseData.getDocumentDisclosureListRes(),
                    caseData.getDocumentDisclosureListRes2());
        List<Element<UploadEvidenceDocumentType>> evidenceDocToAdd =
                deepCopyUploadEvidenceDocumentType(evidenceDocToCopy, RESPONDENT_TWO_DISCLOSURE_LIST);
        builder.documentDisclosureListRes2(evidenceDocToAdd);

        evidenceDocToCopy = compareAndCopy(caseDataBefore.getDocumentForDisclosureRes(),
                                caseData.getDocumentForDisclosureRes(),
                                caseData.getDocumentForDisclosureRes2());
        evidenceDocToAdd = deepCopyUploadEvidenceDocumentType(evidenceDocToCopy, RESPONDENT_TWO_DISCLOSURE);
        builder.documentForDisclosureRes2(evidenceDocToAdd);

        List<Element<UploadEvidenceWitness>> witnessDocToCopy =
                compareAndCopy(caseDataBefore.getDocumentWitnessStatementRes(),
                    caseData.getDocumentWitnessStatementRes(),
                    caseData.getDocumentWitnessStatementRes2());
        List<Element<UploadEvidenceWitness>> witnessDocToAdd =
                deepCopyUploadEvidenceWitness(witnessDocToCopy, RESPONDENT_TWO_WITNESS_STATEMENT);
        builder.documentWitnessStatementRes2(witnessDocToAdd);

        witnessDocToCopy =
                compareAndCopy(caseDataBefore.getDocumentWitnessSummaryRes(),
                        caseData.getDocumentWitnessSummaryRes(),
                        caseData.getDocumentWitnessSummaryRes2());
        witnessDocToAdd =
                deepCopyUploadEvidenceWitness(witnessDocToCopy, RESPONDENT_TWO_WITNESS_SUMMARY);
        builder.documentWitnessSummaryRes2(witnessDocToAdd);

        witnessDocToCopy =
                compareAndCopy(caseDataBefore.getDocumentHearsayNoticeRes(),
                        caseData.getDocumentHearsayNoticeRes(),
                        caseData.getDocumentHearsayNoticeRes2());
        witnessDocToAdd =
                deepCopyUploadEvidenceWitness(witnessDocToCopy, RESPONDENT_TWO_WITNESS_HEARSAY);
        builder.documentHearsayNoticeRes2(witnessDocToAdd);

        evidenceDocToCopy = compareAndCopy(caseDataBefore.getDocumentReferredInStatementRes(),
                caseData.getDocumentReferredInStatementRes(),
                caseData.getDocumentReferredInStatementRes2());
        evidenceDocToAdd = deepCopyUploadEvidenceDocumentType(evidenceDocToCopy, RESPONDENT_TWO_WITNESS_REFERRED);
        builder.documentReferredInStatementRes2(evidenceDocToAdd);

        List<Element<UploadEvidenceExpert>> expertDocToCopy =
                compareAndCopy(caseDataBefore.getDocumentExpertReportRes(),
                        caseData.getDocumentExpertReportRes(),
                        caseData.getDocumentExpertReportRes2());
        List<Element<UploadEvidenceExpert>> expertDocToAdd =
                deepCopyUploadEvidenceExpert(expertDocToCopy, RESPONDENT_TWO_EXPERT_REPORT);
        builder.documentExpertReportRes2(expertDocToAdd);

        expertDocToCopy =
                compareAndCopy(caseDataBefore.getDocumentJointStatementRes(),
                        caseData.getDocumentJointStatementRes(),
                        caseData.getDocumentJointStatementRes2());
        expertDocToAdd =
                deepCopyUploadEvidenceExpert(expertDocToCopy, RESPONDENT_TWO_EXPERT_JOINT_STATEMENT);
        builder.documentJointStatementRes2(expertDocToAdd);

        expertDocToCopy =
                compareAndCopy(caseDataBefore.getDocumentQuestionsRes(),
                        caseData.getDocumentQuestionsRes(),
                        caseData.getDocumentQuestionsRes2());
        expertDocToAdd =
                deepCopyUploadEvidenceExpert(expertDocToCopy, RESPONDENT_TWO_EXPERT_QUESTIONS);
        builder.documentQuestionsRes2(expertDocToAdd);

        expertDocToCopy =
                compareAndCopy(caseDataBefore.getDocumentAnswersRes(),
                        caseData.getDocumentAnswersRes(),
                        caseData.getDocumentAnswersRes2());
        expertDocToAdd =
                deepCopyUploadEvidenceExpert(expertDocToCopy, RESPONDENT_TWO_EXPERT_ANSWERS);
        builder.documentAnswersRes2(expertDocToAdd);

        evidenceDocToCopy = compareAndCopy(caseDataBefore.getDocumentCaseSummaryRes(),
                caseData.getDocumentCaseSummaryRes(),
                caseData.getDocumentCaseSummaryRes2());
        evidenceDocToAdd = deepCopyUploadEvidenceDocumentType(evidenceDocToCopy, RESPONDENT_TWO_PRE_TRIAL_SUMMARY);
        builder.documentCaseSummaryRes2(evidenceDocToAdd);

        evidenceDocToCopy = compareAndCopy(caseDataBefore.getDocumentSkeletonArgumentRes(),
                caseData.getDocumentSkeletonArgumentRes(),
                caseData.getDocumentSkeletonArgumentRes2());
        evidenceDocToAdd = deepCopyUploadEvidenceDocumentType(evidenceDocToCopy, RESPONDENT_TWO_TRIAL_SKELETON);
        builder.documentSkeletonArgumentRes2(evidenceDocToAdd);

        evidenceDocToCopy = compareAndCopy(caseDataBefore.getDocumentAuthoritiesRes(),
                caseData.getDocumentAuthoritiesRes(),
                caseData.getDocumentAuthoritiesRes2());
        evidenceDocToAdd = deepCopyUploadEvidenceDocumentType(evidenceDocToCopy, RESPONDENT_TWO_PRECEDENT_H);
        builder.documentAuthoritiesRes2(evidenceDocToAdd);

        evidenceDocToCopy = compareAndCopy(caseDataBefore.getDocumentCostsRes(),
                caseData.getDocumentCostsRes(),
                caseData.getDocumentCostsRes2());
        evidenceDocToAdd = deepCopyUploadEvidenceDocumentType(evidenceDocToCopy, RESPONDENT_TWO_SCHEDULE_OF_COSTS);
        builder.documentCostsRes2(evidenceDocToAdd);

        evidenceDocToCopy = compareAndCopy(caseDataBefore.getDocumentEvidenceForTrialRes(),
                caseData.getDocumentEvidenceForTrialRes(),
                caseData.getDocumentEvidenceForTrialRes2());
        evidenceDocToAdd = deepCopyUploadEvidenceDocumentType(evidenceDocToCopy, RESPONDENT_TWO_TRIAL_DOC_CORRESPONDENCE);
        builder.documentEvidenceForTrialRes2(evidenceDocToAdd);

        return builder.build();
    }

    private CaseData copyApp1ChangesToApp2(CaseData caseData, CaseData.CaseDataBuilder<?, ?> builder) {
        CaseData caseDataBefore = caseDetailsConverter
                .toCaseData(coreCaseDataService.getCase(caseData.getCcdCaseReference()));
        List<Element<UploadEvidenceDocumentType>> evidenceDocToCopy =
                compareAndCopy(caseDataBefore.getDocumentDisclosureList(),
                        caseData.getDocumentDisclosureList(),
                        caseData.getDocumentDisclosureListApp2());
        List<Element<UploadEvidenceDocumentType>> evidenceDocToAdd =
                deepCopyUploadEvidenceDocumentType(evidenceDocToCopy, APPLICANT_TWO_DISCLOSURE_LIST);
        builder.documentDisclosureListApp2(evidenceDocToAdd);

        evidenceDocToCopy = compareAndCopy(caseDataBefore.getDocumentForDisclosure(),
                caseData.getDocumentForDisclosure(),
                caseData.getDocumentForDisclosureApp2());
        evidenceDocToAdd = deepCopyUploadEvidenceDocumentType(evidenceDocToCopy, APPLICANT_TWO_DISCLOSURE);
        builder.documentForDisclosureApp2(evidenceDocToAdd);

        List<Element<UploadEvidenceWitness>> witnessDocToCopy =
                compareAndCopy(caseDataBefore.getDocumentWitnessStatement(),
                        caseData.getDocumentWitnessStatement(),
                        caseData.getDocumentWitnessStatementApp2());
        List<Element<UploadEvidenceWitness>> witnessDocToAdd =
                deepCopyUploadEvidenceWitness(witnessDocToCopy, APPLICANT_TWO_WITNESS_STATEMENT);
        builder.documentWitnessStatementApp2(witnessDocToAdd);

        witnessDocToCopy =
                compareAndCopy(caseDataBefore.getDocumentWitnessSummary(),
                        caseData.getDocumentWitnessSummary(),
                        caseData.getDocumentWitnessSummaryApp2());
        witnessDocToAdd =
                deepCopyUploadEvidenceWitness(witnessDocToCopy, APPLICANT_TWO_WITNESS_SUMMARY);
        builder.documentWitnessSummaryApp2(witnessDocToAdd);

        witnessDocToCopy =
                compareAndCopy(caseDataBefore.getDocumentHearsayNotice(),
                        caseData.getDocumentHearsayNotice(),
                        caseData.getDocumentHearsayNoticeApp2());
        witnessDocToAdd =
                deepCopyUploadEvidenceWitness(witnessDocToCopy, APPLICANT_TWO_WITNESS_HEARSAY);
        builder.documentHearsayNoticeApp2(witnessDocToAdd);

        evidenceDocToCopy = compareAndCopy(caseDataBefore.getDocumentReferredInStatement(),
                caseData.getDocumentReferredInStatement(),
                caseData.getDocumentReferredInStatementApp2());
        evidenceDocToAdd = deepCopyUploadEvidenceDocumentType(evidenceDocToCopy, APPLICANT_TWO_WITNESS_REFERRED);
        builder.documentReferredInStatementApp2(evidenceDocToAdd);

        List<Element<UploadEvidenceExpert>> expertDocToCopy =
                compareAndCopy(caseDataBefore.getDocumentExpertReport(),
                        caseData.getDocumentExpertReport(),
                        caseData.getDocumentExpertReportApp2());
        List<Element<UploadEvidenceExpert>> expertDocToAdd =
                deepCopyUploadEvidenceExpert(expertDocToCopy, APPLICANT_TWO_EXPERT_REPORT);
        builder.documentExpertReportApp2(expertDocToAdd);

        expertDocToCopy =
                compareAndCopy(caseDataBefore.getDocumentJointStatement(),
                        caseData.getDocumentJointStatement(),
                        caseData.getDocumentJointStatementApp2());
        expertDocToAdd =
                deepCopyUploadEvidenceExpert(expertDocToCopy, APPLICANT_TWO_EXPERT_JOINT_STATEMENT);
        builder.documentJointStatementApp2(expertDocToAdd);

        expertDocToCopy =
                compareAndCopy(caseDataBefore.getDocumentQuestions(),
                        caseData.getDocumentQuestions(),
                        caseData.getDocumentQuestionsApp2());
        expertDocToAdd =
                deepCopyUploadEvidenceExpert(expertDocToCopy, APPLICANT_TWO_EXPERT_QUESTIONS);
        builder.documentQuestionsApp2(expertDocToAdd);

        expertDocToCopy =
                compareAndCopy(caseDataBefore.getDocumentAnswers(),
                        caseData.getDocumentAnswers(),
                        caseData.getDocumentAnswersApp2());
        expertDocToAdd =
                deepCopyUploadEvidenceExpert(expertDocToCopy, APPLICANT_TWO_EXPERT_ANSWERS);
        builder.documentAnswersApp2(expertDocToAdd);

        evidenceDocToCopy = compareAndCopy(caseDataBefore.getDocumentCaseSummary(),
                caseData.getDocumentCaseSummary(),
                caseData.getDocumentCaseSummaryApp2());
        evidenceDocToAdd = deepCopyUploadEvidenceDocumentType(evidenceDocToCopy, APPLICANT_TWO_PRE_TRIAL_SUMMARY);
        builder.documentCaseSummaryApp2(evidenceDocToAdd);

        evidenceDocToCopy = compareAndCopy(caseDataBefore.getDocumentSkeletonArgument(),
                caseData.getDocumentSkeletonArgument(),
                caseData.getDocumentSkeletonArgumentApp2());
        evidenceDocToAdd = deepCopyUploadEvidenceDocumentType(evidenceDocToCopy, APPLICANT_TWO_TRIAL_SKELETON);
        builder.documentSkeletonArgumentApp2(evidenceDocToAdd);

        evidenceDocToCopy = compareAndCopy(caseDataBefore.getDocumentAuthorities(),
                caseData.getDocumentAuthorities(),
                caseData.getDocumentAuthoritiesApp2());
        evidenceDocToAdd = deepCopyUploadEvidenceDocumentType(evidenceDocToCopy, APPLICANT_TWO_PRECEDENT_H);
        builder.documentAuthoritiesApp2(evidenceDocToAdd);

        evidenceDocToCopy = compareAndCopy(caseDataBefore.getDocumentCosts(),
                caseData.getDocumentCosts(),
                caseData.getDocumentCostsApp2());
        evidenceDocToAdd = deepCopyUploadEvidenceDocumentType(evidenceDocToCopy, APPLICANT_TWO_SCHEDULE_OF_COSTS);
        builder.documentCostsApp2(evidenceDocToAdd);

        evidenceDocToCopy = compareAndCopy(caseDataBefore.getDocumentEvidenceForTrial(),
                caseData.getDocumentEvidenceForTrial(),
                caseData.getDocumentEvidenceForTrialApp2());
        evidenceDocToAdd = deepCopyUploadEvidenceDocumentType(evidenceDocToCopy, APPLICANT_TWO_TRIAL_DOC_CORRESPONDENCE);
        builder.documentEvidenceForTrialApp2(evidenceDocToAdd);

        return builder.build();
    }

    private List<Element<UploadEvidenceDocumentType>> deepCopyUploadEvidenceDocumentType(
            final List<Element<UploadEvidenceDocumentType>> toCopy, String theId) {
        if (Objects.isNull(toCopy)) {
            return null;
        }
        List<Element<UploadEvidenceDocumentType>> toAdd = new ArrayList<>();
        for (Element<UploadEvidenceDocumentType> from : toCopy) {
            Document newDoc = Document.builder()
                    .categoryID(theId)
                    .documentBinaryUrl(from.getValue().getDocumentUpload().getDocumentBinaryUrl())
                    .documentFileName(from.getValue().getDocumentUpload().getDocumentFileName())
                    .documentHash(from.getValue().getDocumentUpload().getDocumentHash())
                    .documentUrl(from.getValue().getDocumentUpload().getDocumentUrl())
                    .build();
            UploadEvidenceDocumentType type = UploadEvidenceDocumentType.builder()
                    .witnessOptionName(from.getValue().getWitnessOptionName())
                    .documentIssuedDate(from.getValue().getDocumentIssuedDate())
                    .typeOfDocument(from.getValue().getTypeOfDocument())
                    .createdDatetime(from.getValue().getCreatedDatetime())
                    .documentUpload(newDoc)
                    .build();
            toAdd.add(ElementUtils.element(type));
        }
        return toAdd;
    }

    private List<Element<UploadEvidenceExpert>> deepCopyUploadEvidenceExpert(
            final List<Element<UploadEvidenceExpert>> toCopy, String theId) {
        if (Objects.isNull(toCopy)) {
            return null;
        }
        List<Element<UploadEvidenceExpert>> toAdd = new ArrayList<>();
        for (Element<UploadEvidenceExpert> from : toCopy) {
            Document newDoc = Document.builder()
                    .categoryID(theId)
                    .documentBinaryUrl(from.getValue().getExpertDocument().getDocumentBinaryUrl())
                    .documentFileName(from.getValue().getExpertDocument().getDocumentFileName())
                    .documentHash(from.getValue().getExpertDocument().getDocumentHash())
                    .documentUrl(from.getValue().getExpertDocument().getDocumentUrl())
                    .build();
            UploadEvidenceExpert type = UploadEvidenceExpert.builder()
                    .expertOptionName(from.getValue().getExpertOptionName())
                    .expertOptionExpertise(from.getValue().getExpertOptionExpertise())
                    .expertOptionExpertises(from.getValue().getExpertOptionExpertises())
                    .expertOptionOtherParty(from.getValue().getExpertOptionOtherParty())
                    .expertDocumentQuestion(from.getValue().getExpertDocumentQuestion())
                    .expertDocumentAnswer(from.getValue().getExpertDocumentAnswer())
                    .expertOptionUploadDate(from.getValue().getExpertOptionUploadDate())
                    .expertDocument(newDoc)
                    .build();
            toAdd.add(ElementUtils.element(type));
        }
        return toAdd;
    }

    private List<Element<UploadEvidenceWitness>> deepCopyUploadEvidenceWitness(
            final List<Element<UploadEvidenceWitness>> toCopy, String theId) {
        if (Objects.isNull(toCopy)) {
            return null;
        }
        List<Element<UploadEvidenceWitness>> toAdd = new ArrayList<>();
        for (Element<UploadEvidenceWitness> from : toCopy) {
            Document newDoc = Document.builder()
                    .categoryID(theId)
                    .documentBinaryUrl(from.getValue().getWitnessOptionDocument().getDocumentBinaryUrl())
                    .documentFileName(from.getValue().getWitnessOptionDocument().getDocumentFileName())
                    .documentHash(from.getValue().getWitnessOptionDocument().getDocumentHash())
                    .documentUrl(from.getValue().getWitnessOptionDocument().getDocumentUrl())
                    .build();
            UploadEvidenceWitness type = UploadEvidenceWitness.builder()
                    .witnessOptionUploadDate(from.getValue().getWitnessOptionUploadDate())
                    .witnessOptionName(from.getValue().getWitnessOptionName())
                    .createdDatetime(from.getValue().getCreatedDatetime())
                    .witnessOptionDocument(newDoc)
                    .build();
            toAdd.add(ElementUtils.element(type));
        }
        return toAdd;
    }

    protected static <T> List<Element<T>> compareAndCopy(List<Element<T>> before,
                                    List<Element<T>> after, List<Element<T>> target) {
        if (Objects.isNull(after) || after.isEmpty()) {
            return null;
        }
        List<Element<T>> different = new ArrayList<>();
        if (Objects.isNull(before)) {
            different = after;
        } else {
            List<UUID> ids = before.stream().map(Element::getId).toList();
            for (Element<T> element : after) {
                if (!ids.contains(element.getId())) {
                    different.add(element);
                }
            }
        }
        if (Objects.isNull(target)) {
            target = different;
        } else {
            target.addAll(different);
        }
        return target;
    }

    private String getSelectedRole(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        boolean multiParts = Objects.nonNull(caseData.getEvidenceUploadOptions())
                && !caseData.getEvidenceUploadOptions().getListItems().isEmpty();
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        if (events.get(0).equals(EVIDENCE_UPLOAD_APPLICANT)) {
            if (multiParts && caseData.getEvidenceUploadOptions()
                    .getValue().getLabel().startsWith(OPTION_APP2)) {
                return "APPLICANTSOLICITORTWO";
            }
            if (multiParts && caseData.getEvidenceUploadOptions()
                    .getValue().getLabel().equals(OPTION_APP_BOTH)) {
                return SELECTED_VALUE_APP_BOTH;
            }
            return CaseRole.APPLICANTSOLICITORONE.name();
        } else {
            if ((multiParts && caseData.getEvidenceUploadOptions()
                    .getValue().getLabel().startsWith(OPTION_DEF2))
                || (!multiParts
                    && coreCaseUserService.userHasCaseRole(caseData.getCcdCaseReference().toString(),
                    userInfo.getUid(), RESPONDENTSOLICITORTWO))) {
                return CaseRole.RESPONDENTSOLICITORTWO.name();
            }
            if (multiParts && caseData.getEvidenceUploadOptions()
                    .getValue().getLabel().equals(OPTION_DEF_BOTH)) {
                return SELECTED_VALUE_DEF_BOTH;
            }
            return CaseRole.RESPONDENTSOLICITORONE.name();
        }
    }

    SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Documents uploaded")
            .confirmationBody("You can continue uploading documents or return later. To upload more "
                                  + "documents, go to Next step and select \"Document Upload\".")
            .build();
    }

    void addUploadDocList(List<Element<UploadEvidenceDocumentType>> uploadedEvidenceAfterBundle,
                                  List<Element<UploadEvidenceDocumentType>> documentUploadEvidenceType,
                                  Optional<Bundle> bundleDetails, String docType) {
        if (null == documentUploadEvidenceType) {
            return;
        }
        documentUploadEvidenceType.forEach(uploadEvidenceDocumentType -> {
            if (null != uploadEvidenceDocumentType.getValue().getCreatedDatetime()
                && bundleDetails.get().getCreatedOn().isPresent()
                && uploadEvidenceDocumentType.getValue().getCreatedDatetime()
                .isAfter(bundleDetails.get().getCreatedOn().get())) {
                uploadedEvidenceAfterBundle.add(ElementUtils.element(UploadEvidenceDocumentType.builder()
                                                                         .witnessOptionName(uploadEvidenceDocumentType.getValue().getWitnessOptionName())
                                                                         .typeOfDocument(docType)
                                                                         .createdDatetime(uploadEvidenceDocumentType.getValue().getCreatedDatetime())
                                                                         .documentUpload(uploadEvidenceDocumentType.getValue().getDocumentUpload())
                                                                         .build()));
            }
        });
    }

    void addWitnessDocList(List<Element<UploadEvidenceDocumentType>> uploadedEvidenceAfterBundle,
                                   List<Element<UploadEvidenceWitness>> documentUploadEvidenceType,
                                   Optional<Bundle> bundleDetails, String docType) {
        if (null == documentUploadEvidenceType) {
            return;
        }
        documentUploadEvidenceType.forEach(uploadEvidenceDocumentTypeElement -> {
            if (uploadEvidenceDocumentTypeElement.getValue().getCreatedDatetime().isAfter(bundleDetails.get().getCreatedOn().orElse(
                null))) {
                uploadedEvidenceAfterBundle.add(ElementUtils.element(UploadEvidenceDocumentType.builder()
                                                                         .typeOfDocument(docType)
                                                                         .createdDatetime(
                                                                             uploadEvidenceDocumentTypeElement.getValue().getCreatedDatetime())
                                                                         .documentUpload(
                                                                             uploadEvidenceDocumentTypeElement.getValue().getWitnessOptionDocument())
                                                                         .build()));
            }
        });
    }

    void addExpertDocList(List<Element<UploadEvidenceDocumentType>> uploadedEvidenceAfterBundle,
                                  List<Element<UploadEvidenceExpert>> documentUploadEvidenceType,
                                  Optional<Bundle> bundleDetails, String docType) {
        if (null == documentUploadEvidenceType) {
            return;
        }
        documentUploadEvidenceType.forEach(uploadEvidenceDocumentTypeElement -> {
            if (uploadEvidenceDocumentTypeElement.getValue().getCreatedDatetime().isAfter(bundleDetails.get().getCreatedOn().orElse(
                null))) {
                uploadedEvidenceAfterBundle.add(ElementUtils.element(UploadEvidenceDocumentType.builder()
                                                                         .typeOfDocument(docType)
                                                                         .createdDatetime(
                                                                             uploadEvidenceDocumentTypeElement.getValue().getCreatedDatetime())
                                                                         .documentUpload(
                                                                             uploadEvidenceDocumentTypeElement.getValue().getExpertDocument())
                                                                         .build()));
            }
        });
    }
}
