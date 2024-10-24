package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadExpert;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadTrial;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadWitness;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.DocumentUploadTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.SetOptionsTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_APPLICANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_RESPONDENT;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;

abstract class EvidenceUploadHandlerBase extends CallbackHandler {

    private final List<CaseEvent> events;
    private final String pageId;
    private final String createShowCondition;
    protected final ObjectMapper objectMapper;
    private final CoreCaseUserService coreCaseUserService;
    private final UserService userService;
    protected static final String OPTION_APP1 = "Claimant 1 - ";
    protected static final String OPTION_APP2 = "Claimant 2 - ";
    protected static final String OPTION_APP_BOTH = "Claimants 1 and 2";
    protected static final String OPTION_DEF1 = "Defendant 1 - ";
    protected static final String OPTION_DEF2 = "Defendant 2 - ";
    protected static final String OPTION_DEF_BOTH = "Defendant 1 and 2";
    private static final String CASE_TYPE_FLAG_NO = "do_not_show";

    private static final String SELECTED_VALUE_DEF_BOTH = "RESPONDENTBOTH";
    private static final String SELECTED_VALUE_APP_BOTH = "APPLICANTBOTH";

    private final SetOptionsTask setOptionsTask;
    private final DocumentUploadTask documentUploadTask;

    @SuppressWarnings("java:S107")
    protected EvidenceUploadHandlerBase(UserService userService, CoreCaseUserService coreCaseUserService,
                                        ObjectMapper objectMapper, List<CaseEvent> events, String pageId,
                                        String createShowCondition, SetOptionsTask setOptionsTask, DocumentUploadTask documentUploadTask) {
        this.objectMapper = objectMapper;
        this.createShowCondition = createShowCondition;
        this.events = events;
        this.pageId = pageId;
        this.coreCaseUserService = coreCaseUserService;
        this.userService = userService;
        this.setOptionsTask = setOptionsTask;
        this.documentUploadTask = documentUploadTask;
    }

    abstract CallbackResponse validateValues(CallbackParams callbackParams, CaseData caseData);

    abstract CallbackResponse createShowCondition(CaseData caseData, UserInfo userInfo);

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
        return setOptionsTask.setOptions(callbackParams.getCaseData());
    }

    CallbackResponse createShow(CallbackParams callbackParams) {
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        return createShowCondition(callbackParams.getCaseData(), userInfo);
    }

    // CCD has limited show hide functionality, we want to show a field based on a fixed listed containing an element,
    // or a second list containing an element, AND with the addition of the user being respondent2 solicitor, the below
    // combines the list condition into one single condition, which can then be used in CCD along with the
    // caseTypeFlag condition
    @SuppressWarnings("java:S107")
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
        } else if (events.get(0).equals(EVIDENCE_UPLOAD_RESPONDENT)
            && ((multiParts && caseData.getEvidenceUploadOptions().getValue().getLabel().startsWith(OPTION_DEF2))
            || (!multiParts && Objects.nonNull(caseData.getCcdCaseReference())
            && coreCaseUserService.userHasCaseRole(caseData.getCcdCaseReference().toString(), userInfo.getUid(), RESPONDENTSOLICITORTWO)))) {
            // 1v2 same sol, def2 selected OR 1v2 dif sol, log in as def2
            caseDataBuilder.caseTypeFlag("RespondentTwoFields");
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

    @SuppressWarnings("java:S107")
    CallbackResponse validateValuesParty(List<Element<UploadEvidenceDocumentType>> uploadEvidenceDocumentType,
                                         List<Element<UploadEvidenceWitness>> uploadEvidenceWitness1,
                                         List<Element<UploadEvidenceWitness>> uploadEvidenceWitness2,
                                         List<Element<UploadEvidenceWitness>> uploadEvidenceWitness3,
                                         List<Element<UploadEvidenceDocumentType>> witnessDocumentReferred,
                                         List<Element<UploadEvidenceExpert>> uploadEvidenceExpert1,
                                         List<Element<UploadEvidenceExpert>> uploadEvidenceExpert2,
                                         List<Element<UploadEvidenceExpert>> uploadEvidenceExpert3,
                                         List<Element<UploadEvidenceExpert>> uploadEvidenceExpert4,
                                         List<Element<UploadEvidenceDocumentType>> trialDocumentEvidence,
                                         List<Element<UploadEvidenceDocumentType>> bundleEvidence) {
        List<String> errors = new ArrayList<>();

        checkDateCorrectness(errors, uploadEvidenceDocumentType, date -> date.getValue()
                .getDocumentIssuedDate(),
            "Invalid date: \"Documents for disclosure\" "
                + "date entered must not be in the future (1).");

        checkDateCorrectness(errors, uploadEvidenceWitness1, date -> date.getValue()
                .getWitnessOptionUploadDate(),
            "Invalid date: \"witness statement\" "
                + "date entered must not be in the future (2).");

        checkDateCorrectness(errors, uploadEvidenceWitness2, date -> date.getValue()
                .getWitnessOptionUploadDate(),
            "Invalid date: \"witness summary\" "
                + "date entered must not be in the future (3).");

        checkDateCorrectness(errors, uploadEvidenceWitness3, date -> date.getValue()
                .getWitnessOptionUploadDate(),
            "Invalid date: \"Notice of the intention to rely on hearsay evidence\" "
                + "date entered must not be in the future (4).");

        checkDateCorrectness(errors, witnessDocumentReferred, date -> date.getValue()
                .getDocumentIssuedDate(),
            "Invalid date: \"Documents referred to in the statement\" "
                + "date entered must not be in the future (5).");

        checkDateCorrectness(errors, uploadEvidenceExpert1, date -> date.getValue()
                .getExpertOptionUploadDate(),
            "Invalid date: \"Expert's report\""
                + " date entered must not be in the future (6).");
        checkDateCorrectness(errors, uploadEvidenceExpert2, date -> date.getValue()
                .getExpertOptionUploadDate(),
            "Invalid date: \"Joint statement of experts\" "
                + "date entered must not be in the future (7).");
        checkDateCorrectness(errors, uploadEvidenceExpert3, date -> date.getValue()
                .getExpertOptionUploadDate(),
            "Invalid date: \"Questions for other party's expert or joint experts\" "
                + "expert statement date entered must not be in the future (8).");
        checkDateCorrectness(errors, uploadEvidenceExpert4, date -> date.getValue()
                .getExpertOptionUploadDate(),
            "Invalid date: \"Answers to questions asked by the other party\" "
                + "date entered must not be in the future (9).");

        checkDateCorrectness(errors, trialDocumentEvidence, date -> date.getValue()
                .getDocumentIssuedDate(),
            "Invalid date: \"Documentary evidence for trial\" "
                + "date entered must not be in the future (10).");

        checkDateCorrectnessFuture(errors, bundleEvidence, date -> date.getValue()
                .getDocumentIssuedDate(),
            "Invalid date: \"Bundle Hearing date\" "
                + "date entered must not be in the past (11).");

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    <T> void checkDateCorrectness(List<String> errors, List<Element<T>> documentUpload,
                                  Function<Element<T>, LocalDate> dateExtractor, String errorMessage) {
        if (documentUpload == null) {
            return;
        }
        documentUpload.forEach(date -> {
            LocalDate dateToCheck = dateExtractor.apply(date);
            if (dateToCheck.isAfter(LocalDateTime.now().toLocalDate())) {
                errors.add(errorMessage);
            }
        });
    }

    <T> void checkDateCorrectnessFuture(List<String> errors, List<Element<T>> documentUpload,
                                        Function<Element<T>, LocalDate> dateExtractor, String errorMessage) {
        if (documentUpload == null) {
            return;
        }
        documentUpload.forEach(date -> {
            LocalDate dateToCheck = dateExtractor.apply(date);
            if (dateToCheck.isBefore(LocalDateTime.now().toLocalDate())) {
                errors.add(errorMessage);
            }
        });
    }

    CallbackResponse documentUploadTime(CallbackParams callbackParams) {
        String selectedRole = getSelectedRole(callbackParams);
        return documentUploadTask.uploadDocuments(callbackParams.getCaseData(), callbackParams.getCaseDataBefore(), selectedRole);
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

}
