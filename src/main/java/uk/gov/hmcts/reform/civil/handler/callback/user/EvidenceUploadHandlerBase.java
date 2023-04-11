package uk.gov.hmcts.reform.civil.handler.callback.user;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.getAllocatedTrack;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;

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

    abstract void updateDocumentListUploadedAfterBundle(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, CaseData caseData);

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

        //determine claim path, and assign to CCD object for show hide functionality
        if (caseData.getClaimType() == null) {
            caseDataBuilder.caseProgAllocatedTrack(getAllocatedTrack(caseData.getTotalClaimAmount(), null).name());
        } else {
            caseDataBuilder.caseProgAllocatedTrack(getAllocatedTrack(caseData.getClaimValue().toPounds(), caseData.getClaimType()).name());
        }
        //For case which are 1v1, 2v1  we show respondent fields for documents to be uploaded,
        //if a case is 1v2 and different solicitors we want to show separate fields for each respondent solicitor i.e.
        //RESPONDENTSOLICITORTWO and RESPONDENTSOLICITORONE
        //if a case is 1v2 with same solicitor they will see respondent 2 fields as they have RESPONDENTSOLICITORTWO role
        //default flag for respondent 1 solicitor
        caseDataBuilder.caseTypeFlag("do_not_show");
        //set flag for respondent2
        if (coreCaseUserService.userHasCaseRole(caseData
                                                   .getCcdCaseReference()
                                                   .toString(), userInfo.getUid(), RESPONDENTSOLICITORTWO)) {

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
                                   List<EvidenceUploadTrial> trialCostsFastTrack,
                                   List<EvidenceUploadTrial> trialCostsSmallTrack,
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
        caseDataBuilder.trialCostsFlag("do_not_show");
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

    public <T> void setCategoryId(List<Element<T>> documentUpload, Function<Element<T>,
        Document> documentExtractor, String theID) {
        if (documentUpload == null) {
            return;
        }
        documentUpload.forEach(document -> {
            Document documentToAddId = documentExtractor.apply(document);
            documentToAddId.setCategoryID(theID);
        });
    }

    CallbackResponse documentUploadTime(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        applyDocumentUploadDate(caseDataBuilder, time.now());
        if (nonNull(caseData.getCaseBundles()) && !caseData.getCaseBundles().isEmpty()) {
            updateDocumentListUploadedAfterBundle(caseDataBuilder, caseData);
        }

        if (coreCaseUserService.userHasCaseRole(caseData.getCcdCaseReference().toString(), userInfo.getUid(), RESPONDENTSOLICITORONE)) {
            setCategoryId(caseData.getDocumentDisclosureListRes(), document -> document.getValue().getDocumentUpload(), "RespondentOneDisclosureList");
            setCategoryId(caseData.getDocumentForDisclosureRes(), document -> document.getValue().getDocumentUpload(), "RespondentOneDisclosure");
            setCategoryId(caseData.getDocumentWitnessStatementRes(), document -> document.getValue().getWitnessOptionDocument(), "RespondentOneWitnessStatement");
            setCategoryId(caseData.getDocumentWitnessSummaryRes(), document -> document.getValue().getWitnessOptionDocument(), "RespondentOneWitnessSummary");
            setCategoryId(caseData.getDocumentHearsayNoticeRes(), document -> document.getValue().getWitnessOptionDocument(), "RespondentOneWitnessHearsay");
            setCategoryId(caseData.getDocumentReferredInStatementRes(), document -> document.getValue().getDocumentUpload(), "RespondentOneWitnessReferred");
            setCategoryId(caseData.getDocumentExpertReportRes(), document -> document.getValue().getExpertDocument(), "RespondentOneExpertReport");
            setCategoryId(caseData.getDocumentJointStatementRes(), document -> document.getValue().getExpertDocument(), "RespondentOneExpertJointStatement");
            setCategoryId(caseData.getDocumentQuestionsRes(), document -> document.getValue().getExpertDocument(), "RespondentOneExpertQuestions");
            setCategoryId(caseData.getDocumentAnswersRes(), document -> document.getValue().getExpertDocument(), "RespondentOneExpertAnswers");
            setCategoryId(caseData.getDocumentCaseSummaryRes(), document -> document.getValue().getDocumentUpload(), "RespondentOnePreTrialSummary");
            setCategoryId(caseData.getDocumentSkeletonArgumentRes(), document -> document.getValue().getDocumentUpload(), "RespondentOneTrialSkeleton");
            setCategoryId(caseData.getDocumentAuthoritiesRes(), document -> document.getValue().getDocumentUpload(), "RespondentOneTrialAuthorities");
            setCategoryId(caseData.getDocumentCostsRes(), document -> document.getValue().getDocumentUpload(), "respondentOneTrialCosts");
            setCategoryId(caseData.getDocumentEvidenceForTrialRes(), document -> document.getValue().getDocumentUpload(), "RespondentOneTrialDocCorrespondence");

        }
        if (coreCaseUserService.userHasCaseRole(caseData.getCcdCaseReference().toString(), userInfo.getUid(), RESPONDENTSOLICITORTWO)) {
            setCategoryId(caseData.getDocumentDisclosureListRes2(), document -> document.getValue().getDocumentUpload(), "RespondentTwoDisclosureList");
            setCategoryId(caseData.getDocumentForDisclosureRes2(), document -> document.getValue().getDocumentUpload(), "RespondentTwoDisclosure");
            setCategoryId(caseData.getDocumentWitnessStatementRes2(), document -> document.getValue().getWitnessOptionDocument(), "RespondentTwoWitnessStatement");
            setCategoryId(caseData.getDocumentWitnessSummaryRes2(), document -> document.getValue().getWitnessOptionDocument(), "RespondentTwoWitnessSummary");
            setCategoryId(caseData.getDocumentHearsayNoticeRes2(), document -> document.getValue().getWitnessOptionDocument(), "RespondentTwoWitnessHearsay");
            setCategoryId(caseData.getDocumentReferredInStatementRes2(), document -> document.getValue().getDocumentUpload(), "RespondentTwoWitnessReferred");
            setCategoryId(caseData.getDocumentExpertReportRes2(), document -> document.getValue().getExpertDocument(), "RespondentTwoExpertReport");
            setCategoryId(caseData.getDocumentJointStatementRes2(), document -> document.getValue().getExpertDocument(), "RespondentTwoExpertJointStatement");
            setCategoryId(caseData.getDocumentQuestionsRes2(), document -> document.getValue().getExpertDocument(), "RespondentTwoExpertQuestions");
            setCategoryId(caseData.getDocumentAnswersRes2(), document -> document.getValue().getExpertDocument(), "RespondentTwoExpertAnswers");
            setCategoryId(caseData.getDocumentCaseSummaryRes2(), document -> document.getValue().getDocumentUpload(), "RespondentTwoPreTrialSummary");
            setCategoryId(caseData.getDocumentSkeletonArgumentRes2(), document -> document.getValue().getDocumentUpload(), "RespondentTwoTrialSkeleton");
            setCategoryId(caseData.getDocumentAuthoritiesRes2(), document -> document.getValue().getDocumentUpload(), "RespondentTwoTrialAuthorities");
            setCategoryId(caseData.getDocumentCostsRes2(), document -> document.getValue().getDocumentUpload(), "respondentTwoTrialCosts");
            setCategoryId(caseData.getDocumentEvidenceForTrialRes2(), document -> document.getValue().getDocumentUpload(), "RespondentTwoTrialDocCorrespondence");

        } else {
            setCategoryId(caseData.getDocumentDisclosureList(), document -> document.getValue().getDocumentUpload(), "ApplicantDisclosureList");
            setCategoryId(caseData.getDocumentForDisclosure(), document -> document.getValue().getDocumentUpload(), "ApplicantDisclosure");
            setCategoryId(caseData.getDocumentWitnessStatement(), document -> document.getValue().getWitnessOptionDocument(), "ApplicantWitnessStatement");
            setCategoryId(caseData.getDocumentWitnessSummary(), document -> document.getValue().getWitnessOptionDocument(), "ApplicantWitnessSummary");
            setCategoryId(caseData.getDocumentHearsayNotice(), document -> document.getValue().getWitnessOptionDocument(), "ApplicantWitnessHearsay");
            setCategoryId(caseData.getDocumentReferredInStatement(), document -> document.getValue().getDocumentUpload(), "ApplicantWitnessReferred");
            setCategoryId(caseData.getDocumentExpertReport(), document -> document.getValue().getExpertDocument(), "ApplicantExpertReport");
            setCategoryId(caseData.getDocumentJointStatement(), document -> document.getValue().getExpertDocument(), "ApplicantExpertJointStatement");
            setCategoryId(caseData.getDocumentQuestions(), document -> document.getValue().getExpertDocument(), "ApplicantExpertQuestions");
            setCategoryId(caseData.getDocumentAnswers(), document -> document.getValue().getExpertDocument(), "ApplicantExpertAnswers");
            setCategoryId(caseData.getDocumentCaseSummary(), document -> document.getValue().getDocumentUpload(), "ApplicantPreTrialSummary");
            setCategoryId(caseData.getDocumentSkeletonArgument(), document -> document.getValue().getDocumentUpload(), "ApplicantTrialSkeleton");
            setCategoryId(caseData.getDocumentAuthorities(), document -> document.getValue().getDocumentUpload(), "ApplicantTrialAuthorities");
            setCategoryId(caseData.getDocumentCosts(), document -> document.getValue().getDocumentUpload(), "ApplicantTrialCosts");
            setCategoryId(caseData.getDocumentEvidenceForTrial(), document -> document.getValue().getDocumentUpload(), "ApplicantTrialDocCorrespondence");
        }

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
