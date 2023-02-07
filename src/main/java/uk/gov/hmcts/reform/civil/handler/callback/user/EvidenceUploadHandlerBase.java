package uk.gov.hmcts.reform.civil.handler.callback.user;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadExpert;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadTrial;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadWitness;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWOSPEC;

abstract class EvidenceUploadHandlerBase extends CallbackHandler {

    private final List<CaseEvent> events;
    private final String pageId;
    private final String createShowCondition;
    private final ObjectMapper objectMapper;
    private final Time time;
    private final CoreCaseUserService coreCaseUserService;
    private final UserService userService;

    protected EvidenceUploadHandlerBase(UserService userService, CoreCaseUserService coreCaseUserService,
                                        ObjectMapper objectMapper, Time time, List<CaseEvent> events, String pageId,
                                        String createShowCondition) {
        this.objectMapper = objectMapper;
        this.time = time;
        this.createShowCondition = createShowCondition;
        this.events = events;
        this.pageId = pageId;
        this.coreCaseUserService = coreCaseUserService;
        this.userService = userService;
    }

    abstract CallbackResponse validateValues(CallbackParams callbackParams, CaseData caseData);

    abstract CallbackResponse createShowCondition(CaseData caseData);

    abstract void applyDocumentUploadDate(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, LocalDateTime now);

    @Override
    public List<CaseEvent> handledEvents() {
        return events;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::getCaseType)
            .put(callbackKey(MID, createShowCondition), this::createShow)
            .put(callbackKey(MID, pageId), this::validate)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::documentUploadTime)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    CallbackResponse getCaseType(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        //For case which are 1v1, 2v1 and 1v2 (same solicitor) we show respondent fields for documents to be uploaded,
        //if a case is 1v2 and different solicitors we want to sure separate fields for each respondent solicitor,
        // below creates a show condition for these fields if user is respondent 2 solicitor
        //default flag for respondent 1 solicitor
        caseDataBuilder.caseTypeFlag("do_not_show");
        //set flag for respondent2
        if (coreCaseUserService.userHasCaseRole(caseData
                                                   .getCcdCaseReference()
                                                   .toString(), userInfo.getUid(), RESPONDENTSOLICITORTWO)
            || coreCaseUserService.userHasCaseRole(caseData
                                                    .getCcdCaseReference()
                                                    .toString(), userInfo.getUid(), RESPONDENTSOLICITORTWOSPEC)) {

            caseDataBuilder.caseTypeFlag("RespondentTwoFields");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    CallbackResponse createShow(CallbackParams callbackParams) {
        return createShowCondition(callbackParams.getCaseData());
    }

    // CCD has limited show hide functionality, we want to show a field based on a fixed listed containing an element,
    // or a second list containing an element, AND with the addition of the user being respondent2 solicitor, the below
    // combines the list condition into one single condition, which can then be used in CCD along with the
    // caseTypeFlag condition
    CallbackResponse showCondition(CaseData caseData, List<EvidenceUploadWitness> witnessStatementFastTrack,
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
                                   List<EvidenceUploadTrial> trialDocumentaryFastTrack,
                                   List<EvidenceUploadTrial> trialDocumentarySmallTrack
                                   ) {

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        // clears the flag, as otherwise if the user returns to previous screen and unselects an option,
        // which was previously selected, the option will still be shown
        caseDataBuilder.witnessStatementFlag("do_not_show");
        caseDataBuilder.witnessSummaryFlag("do_not_show");
        caseDataBuilder.witnessReferredStatementFlag("do_not_show");
        caseDataBuilder.expertReportFlag("do_not_show");
        caseDataBuilder.expertJointFlag("do_not_show");
        caseDataBuilder.trialAuthorityFlag("do_not_show");
        caseDataBuilder.trialDocumentaryFlag("do_not_show");

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
        checkDateCorrectness(time, errors, uploadEvidenceWitness3, date -> date.getValue()
                                 .getWitnessOptionUploadDate(),
                             "Invalid date: \"Notice of the intention to rely on hearsay evidence\" "
                                 + "date entered must not be in the future (3).");

        checkDateCorrectness(time, errors, witnessDocumentReferred, date -> date.getValue()
                                 .getDocumentIssuedDate(),
                             "Invalid date: \"Documents referred to in the statement\" "
                                 + "date entered must not be in the future (4).");

        checkDateCorrectness(time, errors, uploadEvidenceExpert1, date -> date.getValue()
                                 .getExpertOptionUploadDate(),
                             "Invalid date: \"Expert's report\""
                                 + " date entered must not be in the future (5).");
        checkDateCorrectness(time, errors, uploadEvidenceExpert2, date -> date.getValue()
                                 .getExpertOptionUploadDate(),
                             "Invalid date: \"Joint statement of experts\" "
                                 + "date entered must not be in the future (6).");
        checkDateCorrectness(time, errors, uploadEvidenceExpert3, date -> date.getValue()
                                 .getExpertOptionUploadDate(),
                             "Invalid date: \"Questions for other party's expert or joint experts\" "
                                 + "expert statement date entered must not be in the future (7).");
        checkDateCorrectness(time, errors, uploadEvidenceExpert4, date -> date.getValue()
                                 .getExpertOptionUploadDate(),
                             "Invalid date: \"Answers to questions asked by the other party\" "
                                 + "date entered must not be in the future (8).");

        checkDateCorrectness(time, errors, trialDocumentEvidence, date -> date.getValue()
                                 .getDocumentIssuedDate(),
                             "Invalid date: \"Documentary evidence for trial\" "
                                 + "date entered must not be in the future (9).");

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

    CallbackResponse documentUploadTime(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        applyDocumentUploadDate(caseDataBuilder, time.now());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Documents uploaded")
            .confirmationBody("You can continue uploading documents or return later. To upload more "
                                  + "documents, go to Next step and select \"Document Upload\".")
            .build();
    }
}
