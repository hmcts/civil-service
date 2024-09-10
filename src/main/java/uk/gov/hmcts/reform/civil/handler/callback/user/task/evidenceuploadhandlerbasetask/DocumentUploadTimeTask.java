package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceuploadhandlerbasetask;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOADED;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Component
public abstract class DocumentUploadTimeTask {

    private static final String SELECTED_VALUE_DEF_BOTH = "RESPONDENTBOTH";
    protected static final String RESPONDENT_ONE_DISCLOSURE_LIST = "RespondentOneDisclosureList";
    protected static final String DISCLOSURE_LIST_TEXT = "%s - Disclosure list";
    protected static final String RESPONDENT_ONE_DISCLOSURE = "RespondentOneDisclosure";
    protected static final String DISCLOSURE_TEXT = "%s - Documents for disclosure";
    protected static final String RESPONDENT_ONE_WITNESS_STATEMENT = "RespondentOneWitnessStatement";
    protected static final String WITNESS_STATEMENT_TEXT = "%s - Witness statement";
    protected static final String WITNESS_SUMMARY_TEXT = "%s - Witness summary";
    protected static final String RESPONDENT_ONE_WITNESS_HEARSAY = "RespondentOneWitnessHearsay";
    protected static final String RESPONDENT_ONE_WITNESS_SUMMARY = "RespondentOneWitnessSummary";
    protected static final String WITNESS_HEARSAY_TEXT = "%s - Notice of the intention to rely on hearsay evidence";
    protected static final String RESPONDENT_ONE_WITNESS_REFERRED = "RespondentOneWitnessReferred";
    protected static final String WITNESS_REFERRED_TEXT = "%s - Documents referred to in the statement";
    protected static final String RESPONDENT_ONE_EXPERT_REPORT = "RespondentOneExpertReport";
    protected static final String EXPERT_REPORT_TEXT = "%s - Expert's report";
    protected static final String RESPONDENT_ONE_EXPERT_JOINT_STATEMENT = "RespondentOneExpertJointStatement";
    protected static final String EXPERT_JOINT_STATEMENT_TEXT = "%s - Joint Statement of Experts / Single Joint Expert Report";
    protected static final String RESPONDENT_ONE_EXPERT_QUESTIONS = "RespondentOneExpertQuestions";
    protected static final String EXPERT_QUESTIONS_TEXT = "%s - Questions for other party's expert or joint experts";
    protected static final String RESPONDENT_ONE_EXPERT_ANSWERS = "RespondentOneExpertAnswers";
    protected static final String EXPERT_ANSWERS_TEXT = "%s - Answer to questions asked";
    protected static final String RESPONDENT_ONE_PRE_TRIAL_SUMMARY = "RespondentOnePreTrialSummary";
    protected static final String PRE_TRIAL_SUMMARY_TEXT = "%s - Case Summary";
    protected static final String RESPONDENT_ONE_TRIAL_SKELETON = "RespondentOneTrialSkeleton";
    protected static final String TRIAL_SKELETON_TEXT = "%s - Skeleton argument";
    protected static final String TRIAL_AUTHORITIES_TEXT = "%s - Authorities";
    protected static final String RESPONDENT_ONE_SCHEDULE_OF_COSTS = "RespondentSchedulesOfCost";
    protected static final String TRIAL_COSTS_TEXT = "%s - Costs";
    protected static final String RESPONDENT_ONE_TRIAL_DOC_CORRESPONDENCE = "RespondentOneTrialDocCorrespondence";
    protected static final String TRIAL_DOC_CORRESPONDENCE_TEXT = "%s - Documentary evidence for trial";
    protected static final String RESPONDENT_TWO_DISCLOSURE_LIST = "RespondentTwoDisclosureList";
    protected static final String RESPONDENT_TWO_DISCLOSURE = "RespondentTwoDisclosure";
    protected static final String RESPONDENT_TWO_WITNESS_STATEMENT = "RespondentTwoWitnessStatement";
    protected static final String RESPONDENT_TWO_WITNESS_SUMMARY = "RespondentTwoWitnessSummary";
    protected static final String RESPONDENT_TWO_WITNESS_HEARSAY = "RespondentTwoWitnessHearsay";
    protected static final String RESPONDENT_TWO_WITNESS_REFERRED = "RespondentTwoWitnessReferred";
    protected static final String RESPONDENT_TWO_EXPERT_REPORT = "RespondentTwoExpertReport";
    protected static final String RESPONDENT_TWO_EXPERT_JOINT_STATEMENT = "RespondentTwoExpertJointStatement";
    protected static final String RESPONDENT_TWO_EXPERT_QUESTIONS = "RespondentTwoExpertQuestions";
    protected static final String RESPONDENT_TWO_EXPERT_ANSWERS = "RespondentTwoExpertAnswers";
    protected static final String RESPONDENT_TWO_PRE_TRIAL_SUMMARY = "RespondentTwoPreTrialSummary";
    protected static final String RESPONDENT_TWO_TRIAL_SKELETON = "RespondentTwoTrialSkeleton";
    protected static final String RESPONDENT_TWO_SCHEDULE_OF_COSTS = "RespondentTwoSchedulesOfCost";
    protected static final String RESPONDENT_TWO_TRIAL_DOC_CORRESPONDENCE = "RespondentTwoTrialDocCorrespondence";
    private static final String SELECTED_VALUE_APP_BOTH = "APPLICANTBOTH";
    protected static final String APPLICANT_DISCLOSURE_LIST = "ApplicantDisclosureList";
    protected static final String APPLICANT_DISCLOSURE = "ApplicantDisclosure";
    protected static final String APPLICANT_WITNESS_STATEMENT = "ApplicantWitnessStatement";
    protected static final String APPLICANT_WITNESS_SUMMARY = "ApplicantWitnessSummary";
    protected static final String APPLICANT_WITNESS_HEARSAY = "ApplicantWitnessHearsay";
    protected static final String APPLICANT_WITNESS_REFERRED = "ApplicantWitnessReferred";
    protected static final String APPLICANT_EXPERT_REPORT = "ApplicantExpertReport";
    protected static final String APPLICANT_EXPERT_JOINT_STATEMENT = "ApplicantExpertJointStatement";
    protected static final String APPLICANT_EXPERT_QUESTIONS = "ApplicantExpertQuestions";
    protected static final String APPLICANT_EXPERT_ANSWERS = "ApplicantExpertAnswers";
    protected static final String APPLICANT_PRE_TRIAL_SUMMARY = "ApplicantPreTrialSummary";
    protected static final String APPLICANT_TRIAL_SKELETON = "ApplicantTrialSkeleton";
    protected static final String APPLICANT_SCHEDULE_OF_COSTS = "ApplicantSchedulesOfCost";
    protected static final String APPLICANT_TRIAL_DOC_CORRESPONDENCE = "ApplicantTrialDocCorrespondence";
    protected static final String APPLICANT_TWO_DISCLOSURE_LIST = "ApplicantTwoDisclosureList";
    protected static final String APPLICANT_TWO_DISCLOSURE = "ApplicantTwoDisclosure";
    protected static final String APPLICANT_TWO_WITNESS_STATEMENT = "ApplicantTwoWitnessStatement";
    protected static final String APPLICANT_TWO_WITNESS_SUMMARY = "ApplicantTwoWitnessSummary";
    protected static final String APPLICANT_TWO_WITNESS_HEARSAY = "ApplicantTwoWitnessHearsay";
    protected static final String APPLICANT_TWO_WITNESS_REFERRED = "ApplicantTwoWitnessReferred";
    protected static final String APPLICANT_TWO_EXPERT_REPORT = "ApplicantTwoExpertReport";
    protected static final String APPLICANT_TWO_EXPERT_JOINT_STATEMENT = "ApplicantTwoExpertJointStatement";
    protected static final String APPLICANT_TWO_EXPERT_QUESTIONS = "ApplicantTwoExpertQuestions";
    protected static final String APPLICANT_TWO_EXPERT_ANSWERS = "ApplicantTwoExpertAnswers";
    protected static final String APPLICANT_TWO_TRIAL_DOC_CORRESPONDENCE = "ApplicantTwoTrialDocCorrespondence";
    protected static final String APPLICANT_TWO_PRE_TRIAL_SUMMARY = "ApplicantTwoPreTrialSummary";
    protected static final String APPLICANT_TWO_TRIAL_SKELETON = "ApplicantTwoTrialSkeleton";
    protected static final String APPLICANT_TWO_SCHEDULE_OF_COSTS = "ApplicantTwoSchedulesOfCost";
    // Notification Strings used for email
    protected static StringBuilder notificationString = new StringBuilder();
    private static final String SPACE = " ";
    private static final String END = ".";
    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private final Time time;
    private final FeatureToggleService featureToggleService;
    private final ObjectMapper objectMapper;
    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;

    public DocumentUploadTimeTask(Time time, FeatureToggleService featureToggleService,
                                  ObjectMapper objectMapper, CaseDetailsConverter caseDetailsConverter,
                                  CoreCaseDataService coreCaseDataService) {
        this.time = time;
        this.featureToggleService = featureToggleService;
        this.objectMapper = objectMapper;
        this.caseDetailsConverter = caseDetailsConverter;
        this.coreCaseDataService = coreCaseDataService;
    }

    abstract void applyDocumentUploadDate(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, LocalDateTime now);

    abstract void updateDocumentListUploadedAfterBundle(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, CaseData caseData);
    static void getNotificationText(CaseData caseData) {
        notificationString = new StringBuilder();
        if (caseData.getNotificationText() != null) {
            notificationString = new StringBuilder(caseData.getNotificationText());
        }
    }

    private void updateDocumentListUploadedAfterBundle(CaseData caseData, CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        if (nonNull(caseData.getCaseBundles()) && !caseData.getCaseBundles().isEmpty()) {
            updateDocumentListUploadedAfterBundle(caseDataBuilder, caseData);
        }
    }

    public CallbackResponse documentUploadTime(CaseData caseData, String selectedRole) {

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        // If notification has already been populated in current day, we want to append to that existing notification
        getNotificationText(caseData);
        applyDocumentUploadDate(caseDataBuilder, time.now());
        updateDocumentListUploadedAfterBundle(caseData, caseDataBuilder);

        if (selectedRole.equals(RESPONDENTSOLICITORONE.name()) || selectedRole.equals(SELECTED_VALUE_DEF_BOTH)) {
            String defendantString = "Defendant 1";
            if (selectedRole.equals(SELECTED_VALUE_DEF_BOTH)) {
                defendantString = "Both defendants";
            }
            setCategoryIdAndRenameDoc(caseData.getDocumentDisclosureListRes(),
                                      document -> document.getValue().getDocumentUpload(),
                                      RESPONDENT_ONE_DISCLOSURE_LIST,
                                      DISCLOSURE_LIST_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      defendantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentForDisclosureRes(),
                                      document -> document.getValue().getDocumentUpload(),
                                      RESPONDENT_ONE_DISCLOSURE,
                                      DISCLOSURE_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      defendantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentWitnessStatementRes(),
                                      document -> document.getValue().getWitnessOptionDocument(),
                                      RESPONDENT_ONE_WITNESS_STATEMENT,
                                      WITNESS_STATEMENT_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      defendantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentWitnessSummaryRes(),
                                      document -> document.getValue().getWitnessOptionDocument(),
                                      RESPONDENT_ONE_WITNESS_SUMMARY,
                                      WITNESS_SUMMARY_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      defendantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentHearsayNoticeRes(),
                                      document -> document.getValue().getWitnessOptionDocument(),
                                      RESPONDENT_ONE_WITNESS_HEARSAY,
                                      WITNESS_HEARSAY_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      defendantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentReferredInStatementRes(),
                                      document -> document.getValue().getDocumentUpload(),
                                      RESPONDENT_ONE_WITNESS_REFERRED,
                                      WITNESS_REFERRED_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      defendantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentExpertReportRes(),
                                      document -> document.getValue().getExpertDocument(),
                                      RESPONDENT_ONE_EXPERT_REPORT,
                                      EXPERT_REPORT_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      defendantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentJointStatementRes(),
                                      document -> document.getValue().getExpertDocument(),
                                      RESPONDENT_ONE_EXPERT_JOINT_STATEMENT,
                                      EXPERT_JOINT_STATEMENT_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      defendantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentQuestionsRes(),
                                      document -> document.getValue().getExpertDocument(),
                                      RESPONDENT_ONE_EXPERT_QUESTIONS,
                                      EXPERT_QUESTIONS_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      defendantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentAnswersRes(),
                                      document -> document.getValue().getExpertDocument(),
                                      RESPONDENT_ONE_EXPERT_ANSWERS,
                                      EXPERT_ANSWERS_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      defendantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentCaseSummaryRes(),
                                      document -> document.getValue().getDocumentUpload(),
                                      RESPONDENT_ONE_PRE_TRIAL_SUMMARY,
                                      PRE_TRIAL_SUMMARY_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      defendantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentSkeletonArgumentRes(),
                                      document -> document.getValue().getDocumentUpload(),
                                      RESPONDENT_ONE_TRIAL_SKELETON,
                                      TRIAL_SKELETON_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      defendantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentAuthoritiesRes(),
                                      document -> document.getValue().getDocumentUpload(),
                                      RESPONDENT_ONE_TRIAL_SKELETON,
                                      TRIAL_AUTHORITIES_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      defendantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentCostsRes(),
                                      document -> document.getValue().getDocumentUpload(),
                                      RESPONDENT_ONE_SCHEDULE_OF_COSTS,
                                      TRIAL_COSTS_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      defendantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentEvidenceForTrialRes(),
                                      document -> document.getValue().getDocumentUpload(),
                                      RESPONDENT_ONE_TRIAL_DOC_CORRESPONDENCE,
                                      TRIAL_DOC_CORRESPONDENCE_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      defendantString
            );
            if (selectedRole.equals(SELECTED_VALUE_DEF_BOTH)) {
                caseData = copyResp1ChangesToResp2(caseData, caseDataBuilder);
            }
        }
        if (selectedRole.equals(RESPONDENTSOLICITORTWO.name())) {
            String defendantString = "Defendant 2";
            setCategoryIdAndRenameDoc(caseData.getDocumentDisclosureListRes2(),
                                      document -> document.getValue().getDocumentUpload(),
                                      RESPONDENT_TWO_DISCLOSURE_LIST,
                                      DISCLOSURE_LIST_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      defendantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentForDisclosureRes2(),
                                      document -> document.getValue().getDocumentUpload(),
                                      RESPONDENT_TWO_DISCLOSURE,
                                      DISCLOSURE_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      defendantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentWitnessStatementRes2(),
                                      document -> document.getValue().getWitnessOptionDocument(),
                                      RESPONDENT_TWO_WITNESS_STATEMENT,
                                      WITNESS_STATEMENT_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      defendantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentWitnessSummaryRes2(),
                                      document -> document.getValue().getWitnessOptionDocument(),
                                      RESPONDENT_TWO_WITNESS_SUMMARY,
                                      WITNESS_SUMMARY_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      defendantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentHearsayNoticeRes2(),
                                      document -> document.getValue().getWitnessOptionDocument(),
                                      RESPONDENT_TWO_WITNESS_HEARSAY,
                                      WITNESS_HEARSAY_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      defendantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentReferredInStatementRes2(),
                                      document -> document.getValue().getDocumentUpload(),
                                      RESPONDENT_TWO_WITNESS_REFERRED,
                                      WITNESS_REFERRED_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      defendantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentExpertReportRes2(),
                                      document -> document.getValue().getExpertDocument(),
                                      RESPONDENT_TWO_EXPERT_REPORT,
                                      EXPERT_REPORT_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      defendantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentJointStatementRes2(),
                                      document -> document.getValue().getExpertDocument(),
                                      RESPONDENT_TWO_EXPERT_JOINT_STATEMENT,
                                      EXPERT_JOINT_STATEMENT_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      defendantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentQuestionsRes2(),
                                      document -> document.getValue().getExpertDocument(),
                                      RESPONDENT_TWO_EXPERT_QUESTIONS,
                                      EXPERT_QUESTIONS_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      defendantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentAnswersRes2(),
                                      document -> document.getValue().getExpertDocument(),
                                      RESPONDENT_TWO_EXPERT_ANSWERS,
                                      EXPERT_ANSWERS_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      defendantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentCaseSummaryRes2(),
                                      document -> document.getValue().getDocumentUpload(),
                                      RESPONDENT_TWO_PRE_TRIAL_SUMMARY,
                                      PRE_TRIAL_SUMMARY_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      defendantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentSkeletonArgumentRes2(),
                                      document -> document.getValue().getDocumentUpload(),
                                      RESPONDENT_TWO_TRIAL_SKELETON,
                                      TRIAL_SKELETON_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      defendantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentAuthoritiesRes2(),
                                      document -> document.getValue().getDocumentUpload(),
                                      RESPONDENT_TWO_TRIAL_SKELETON,
                                      TRIAL_AUTHORITIES_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      defendantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentCostsRes2(),
                                      document -> document.getValue().getDocumentUpload(),
                                      RESPONDENT_TWO_SCHEDULE_OF_COSTS,
                                      TRIAL_COSTS_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      defendantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentEvidenceForTrialRes2(),
                                      document -> document.getValue().getDocumentUpload(),
                                      RESPONDENT_TWO_TRIAL_DOC_CORRESPONDENCE,
                                      TRIAL_DOC_CORRESPONDENCE_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      defendantString
            );
        }

        if (selectedRole.equals(CaseRole.APPLICANTSOLICITORONE.name()) || selectedRole.equals(SELECTED_VALUE_APP_BOTH)) {
            String claimantString = "Claimant 1";
            if (selectedRole.equals(SELECTED_VALUE_APP_BOTH)) {
                claimantString = "Both claimants";
            }
            setCategoryIdAndRenameDoc(caseData.getDocumentDisclosureList(),
                                      document -> document.getValue().getDocumentUpload(),
                                      APPLICANT_DISCLOSURE_LIST,
                                      DISCLOSURE_LIST_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      claimantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentForDisclosure(),
                                      document -> document.getValue().getDocumentUpload(),
                                      APPLICANT_DISCLOSURE,
                                      DISCLOSURE_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      claimantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentWitnessStatement(),
                                      document -> document.getValue().getWitnessOptionDocument(),
                                      APPLICANT_WITNESS_STATEMENT,
                                      WITNESS_STATEMENT_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      claimantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentWitnessSummary(),
                                      document -> document.getValue().getWitnessOptionDocument(),
                                      APPLICANT_WITNESS_SUMMARY,
                                      WITNESS_SUMMARY_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      claimantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentHearsayNotice(),
                                      document -> document.getValue().getWitnessOptionDocument(),
                                      APPLICANT_WITNESS_HEARSAY,
                                      WITNESS_HEARSAY_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      claimantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentReferredInStatement(),
                                      document -> document.getValue().getDocumentUpload(),
                                      APPLICANT_WITNESS_REFERRED,
                                      WITNESS_REFERRED_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      claimantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentExpertReport(),
                                      document -> document.getValue().getExpertDocument(),
                                      APPLICANT_EXPERT_REPORT,
                                      EXPERT_REPORT_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      claimantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentJointStatement(),
                                      document -> document.getValue().getExpertDocument(),
                                      APPLICANT_EXPERT_JOINT_STATEMENT,
                                      EXPERT_JOINT_STATEMENT_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      claimantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentQuestions(),
                                      document -> document.getValue().getExpertDocument(),
                                      APPLICANT_EXPERT_QUESTIONS,
                                      EXPERT_QUESTIONS_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      claimantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentAnswers(),
                                      document -> document.getValue().getExpertDocument(),
                                      APPLICANT_EXPERT_ANSWERS,
                                      EXPERT_ANSWERS_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      claimantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentCaseSummary(),
                                      document -> document.getValue().getDocumentUpload(),
                                      APPLICANT_PRE_TRIAL_SUMMARY,
                                      PRE_TRIAL_SUMMARY_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      claimantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentSkeletonArgument(),
                                      document -> document.getValue().getDocumentUpload(),
                                      APPLICANT_TRIAL_SKELETON,
                                      TRIAL_SKELETON_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      claimantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentAuthorities(),
                                      document -> document.getValue().getDocumentUpload(),
                                      APPLICANT_TRIAL_SKELETON,
                                      TRIAL_AUTHORITIES_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      claimantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentCosts(),
                                      document -> document.getValue().getDocumentUpload(),
                                      APPLICANT_SCHEDULE_OF_COSTS,
                                      TRIAL_COSTS_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      claimantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentEvidenceForTrial(),
                                      document -> document.getValue().getDocumentUpload(),
                                      APPLICANT_TRIAL_DOC_CORRESPONDENCE,
                                      TRIAL_DOC_CORRESPONDENCE_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      claimantString
            );
            if (selectedRole.equals(SELECTED_VALUE_APP_BOTH)) {
                caseData = copyApp1ChangesToApp2(caseData, caseDataBuilder);
            }
        }

        if (selectedRole.equals("APPLICANTSOLICITORTWO")) {
            String claimantString = "Claimant 2";
            setCategoryIdAndRenameDoc(caseData.getDocumentDisclosureListApp2(),
                                      document -> document.getValue().getDocumentUpload(),
                                      APPLICANT_TWO_DISCLOSURE_LIST,
                                      DISCLOSURE_LIST_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      claimantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentForDisclosureApp2(),
                                      document -> document.getValue().getDocumentUpload(),
                                      APPLICANT_TWO_DISCLOSURE,
                                      DISCLOSURE_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      claimantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentWitnessStatementApp2(),
                                      document -> document.getValue().getWitnessOptionDocument(),
                                      APPLICANT_TWO_WITNESS_STATEMENT,
                                      WITNESS_STATEMENT_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      claimantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentWitnessSummaryApp2(),
                                      document -> document.getValue().getWitnessOptionDocument(),
                                      APPLICANT_TWO_WITNESS_SUMMARY,
                                      WITNESS_SUMMARY_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      claimantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentHearsayNoticeApp2(),
                                      document -> document.getValue().getWitnessOptionDocument(),
                                      APPLICANT_TWO_WITNESS_HEARSAY,
                                      WITNESS_HEARSAY_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      claimantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentReferredInStatementApp2(),
                                      document -> document.getValue().getDocumentUpload(),
                                      APPLICANT_TWO_WITNESS_REFERRED,
                                      WITNESS_REFERRED_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      claimantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentExpertReportApp2(),
                                      document -> document.getValue().getExpertDocument(),
                                      APPLICANT_TWO_EXPERT_REPORT,
                                      EXPERT_REPORT_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      claimantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentJointStatementApp2(),
                                      document -> document.getValue().getExpertDocument(),
                                      APPLICANT_TWO_EXPERT_JOINT_STATEMENT,
                                      EXPERT_JOINT_STATEMENT_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      claimantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentQuestionsApp2(),
                                      document -> document.getValue().getExpertDocument(),
                                      APPLICANT_TWO_EXPERT_QUESTIONS,
                                      EXPERT_QUESTIONS_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      claimantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentAnswersApp2(),
                                      document -> document.getValue().getExpertDocument(),
                                      APPLICANT_TWO_EXPERT_ANSWERS,
                                      EXPERT_ANSWERS_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      claimantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentCaseSummaryApp2(),
                                      document -> document.getValue().getDocumentUpload(),
                                      APPLICANT_TWO_PRE_TRIAL_SUMMARY,
                                      PRE_TRIAL_SUMMARY_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      claimantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentSkeletonArgumentApp2(),
                                      document -> document.getValue().getDocumentUpload(),
                                      APPLICANT_TWO_TRIAL_SKELETON,
                                      TRIAL_SKELETON_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      claimantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentAuthoritiesApp2(),
                                      document -> document.getValue().getDocumentUpload(),
                                      APPLICANT_TWO_TRIAL_SKELETON,
                                      TRIAL_AUTHORITIES_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      claimantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentCostsApp2(),
                                      document -> document.getValue().getDocumentUpload(),
                                      APPLICANT_TWO_SCHEDULE_OF_COSTS,
                                      TRIAL_COSTS_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      claimantString
            );
            setCategoryIdAndRenameDoc(caseData.getDocumentEvidenceForTrialApp2(),
                                      document -> document.getValue().getDocumentUpload(),
                                      APPLICANT_TWO_TRIAL_DOC_CORRESPONDENCE,
                                      TRIAL_DOC_CORRESPONDENCE_TEXT,
                                      documentDateTime -> documentDateTime.getValue().getCreatedDatetime(),
                                      claimantString
            );
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

        if (featureToggleService.isCaseProgressionEnabled()) {
            caseDataBuilder.businessProcess(BusinessProcess.ready(EVIDENCE_UPLOADED));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    public <T> void setCategoryIdAndRenameDoc(List<Element<T>> documentUpload, Function<Element<T>,
        Document> documentExtractor, String theID, String docNotificationText, Function<Element<T>,
        LocalDateTime> documentDateTimeExtractor, String claimantDefendantString) {
        if (documentUpload == null || documentUpload.isEmpty()) {
            return;
        }
        LocalDateTime halfFivePmYesterday = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.of(17, 30));
        renameDocuments(documentUpload, theID);
        documentUpload.forEach(document -> {
            Document documentToAddId = documentExtractor.apply(document);
            documentToAddId.setCategoryID(theID);
            LocalDateTime dateTime = documentDateTimeExtractor.apply(document);
            if (dateTime.isAfter(halfFivePmYesterday)) {
                String updateNotificationText = format(docNotificationText, claimantDefendantString);
                if (!notificationString.toString().contains(updateNotificationText)) {
                    notificationString.append("\n").append(updateNotificationText);
                }
            }
        });
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
        evidenceDocToAdd = deepCopyUploadEvidenceDocumentType(evidenceDocToCopy, RESPONDENT_TWO_TRIAL_SKELETON);
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
        evidenceDocToAdd = deepCopyUploadEvidenceDocumentType(evidenceDocToCopy, APPLICANT_TWO_TRIAL_SKELETON);
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

    <T> void addUploadDocList(List<Element<T>> documentUploaded, Function<Element<T>, Document> documentExtractor, Function<Element<T>,
        LocalDateTime> documentUploadTimeExtractor, CaseData.CaseDataBuilder<?, ?> caseDataBuilder, CaseData caseData,
                              String documentTypeDisplayName, String respondentOrApplicant) {

        if (null == documentUploaded) {
            return;
        }
        Optional<Bundle> bundleDetails = caseData.getCaseBundles().stream().map(IdValue::getValue)
            .max(Comparator.comparing(bundle -> bundle.getCreatedOn().orElse(null)));
        LocalDateTime trialBundleDate = null;
        if (bundleDetails.isPresent()) {
            Optional<LocalDateTime> createdOn = bundleDetails.get().getCreatedOn();
            if (createdOn.isPresent()) {
                trialBundleDate = createdOn.get();
            }
        }
        if (Objects.equals(respondentOrApplicant, "applicant")) {
            populateBundleCollection(
                documentUploaded,
                documentExtractor,
                documentUploadTimeExtractor,
                caseData::getApplicantDocsUploadedAfterBundle,
                caseDataBuilder::applicantDocsUploadedAfterBundle,
                documentTypeDisplayName,
                trialBundleDate
            );
        } else {
            populateBundleCollection(
                documentUploaded,
                documentExtractor,
                documentUploadTimeExtractor,
                caseData::getRespondentDocsUploadedAfterBundle,
                caseDataBuilder::respondentDocsUploadedAfterBundle,
                documentTypeDisplayName,
                trialBundleDate
            );
        }
    }

    private <T> void populateBundleCollection(List<Element<T>> documentUploaded,
                                              Function<Element<T>, Document> documentExtractor,
                                              Function<Element<T>, LocalDateTime> documentUploadTimeExtractor,
                                              Supplier<List<Element<UploadEvidenceDocumentType>>> existingDocsSupplier,
                                              Consumer<List<Element<UploadEvidenceDocumentType>>> docsUpdater,
                                              String documentTypeDisplayName,
                                              LocalDateTime trialBundleDate) {
        List<Element<UploadEvidenceDocumentType>> additionalBundleDocs = null;
        // If either claimant or respondent additional bundle doc collection exists, we add to that
        if (existingDocsSupplier.get() != null) {
            additionalBundleDocs = existingDocsSupplier.get();
        }
        List<Element<UploadEvidenceDocumentType>> finalAdditionalBundleDocs = additionalBundleDocs;
        documentUploaded.forEach(uploadEvidenceDocumentType -> {
            Document documentToAdd = documentExtractor.apply(uploadEvidenceDocumentType);
            LocalDateTime documentCreatedDateTime = documentUploadTimeExtractor.apply(uploadEvidenceDocumentType);
            // If document was uploaded after the trial bundle was created, it is added to additional bundle documents
            // via applicant or respondent collections
            if (documentCreatedDateTime != null
                && documentCreatedDateTime.isAfter(trialBundleDate)
            ) {
                // If a document already exists in the collection, it cannot be re-added.
                boolean containsValue = finalAdditionalBundleDocs.stream()
                    .map(Element::getValue)
                    .map(upload -> upload != null ? upload.getDocumentUpload() : null)
                    .filter(Objects::nonNull)
                    .map(Document::getDocumentUrl)
                    .anyMatch(docUrl -> docUrl.equals(documentToAdd.getDocumentUrl()));
                // When a bundle is created, applicantDocsUploadedAfterBundle and respondentDocsUploadedAfterBundle
                // are assigned as empty lists, in actuality they contain a single element (default builder) we remove
                // this as it is not required.
                finalAdditionalBundleDocs.removeIf(element -> {
                    UploadEvidenceDocumentType upload = element.getValue();
                    return upload == null || upload.getDocumentUpload() == null
                        || upload.getDocumentUpload().getDocumentUrl() == null;
                });
                if (!containsValue) {
                    var newDocument = UploadEvidenceDocumentType.builder()
                        .typeOfDocument(documentTypeDisplayName)
                        .createdDatetime(documentCreatedDateTime)
                        .documentUpload(documentToAdd)
                        .build();
                    finalAdditionalBundleDocs.add(element(newDocument));
                    docsUpdater.accept(finalAdditionalBundleDocs);
                }
            }
        });
    }
}
