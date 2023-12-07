package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_SUBMIT_APPLICANT;

@Service
@RequiredArgsConstructor
public class EvidenceSubmitApplicantHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(EVIDENCE_SUBMIT_APPLICANT);
    private final ObjectMapper objectMapper;


    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::emptyCallbackResponse)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::submitDocuments)
            .put(callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private AboutToStartOrSubmitCallbackResponse submitDocuments(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        // copy applicant one draft documents to submitted documents
        caseDataBuilder.submitDocumentDisclosureList(copyDraftDocumentToSubmitted(caseData.getDocumentDisclosureList()));
        caseDataBuilder.submitDocumentForDisclosure(copyDraftDocumentToSubmitted(caseData.getDocumentForDisclosure()));
        caseDataBuilder.submitDocumentWitnessStatement(copyDraftDocumentToSubmitted(caseData.getDocumentWitnessStatement()));
        caseDataBuilder.submitDocumentWitnessSummary(copyDraftDocumentToSubmitted(caseData.getDocumentWitnessSummary()));
        caseDataBuilder.submitDocumentHearsayNotice(copyDraftDocumentToSubmitted(caseData.getDocumentHearsayNotice()));
        caseDataBuilder.submitDocumentReferredInStatement(copyDraftDocumentToSubmitted(caseData.getDocumentReferredInStatement()));
        caseDataBuilder.submitDocumentExpertReport(copyDraftDocumentToSubmitted(caseData.getDocumentExpertReport()));
        caseDataBuilder.submitDocumentJointStatement(copyDraftDocumentToSubmitted(caseData.getDocumentJointStatement()));
        caseDataBuilder.submitDocumentQuestions(copyDraftDocumentToSubmitted(caseData.getDocumentQuestions()));
        caseDataBuilder.submitDocumentAnswers(copyDraftDocumentToSubmitted(caseData.getDocumentAnswers()));
        caseDataBuilder.submitDocumentCaseSummary(copyDraftDocumentToSubmitted(caseData.getDocumentCaseSummary()));
        caseDataBuilder.submitDocumentSkeletonArgument(copyDraftDocumentToSubmitted(caseData.getDocumentSkeletonArgument()));
        caseDataBuilder.submitDocumentAuthorities(copyDraftDocumentToSubmitted(caseData.getDocumentAuthorities()));
        caseDataBuilder.submitDocumentCosts(copyDraftDocumentToSubmitted(caseData.getDocumentCosts()));
        caseDataBuilder.submitDocumentEvidenceForTrial(copyDraftDocumentToSubmitted(caseData.getDocumentEvidenceForTrial()));

        // copy applicant two draft documents to submitted documents
        caseDataBuilder.submitDocumentDisclosureListApp2(copyDraftDocumentToSubmitted(caseData.getDocumentDisclosureListApp2()));
        caseDataBuilder.submitDocumentForDisclosureApp2(copyDraftDocumentToSubmitted(caseData.getDocumentForDisclosureApp2()));
        caseDataBuilder.submitDocumentWitnessStatementApp2(copyDraftDocumentToSubmitted(caseData.getDocumentWitnessStatementApp2()));
        caseDataBuilder.submitDocumentWitnessSummaryApp2(copyDraftDocumentToSubmitted(caseData.getDocumentWitnessSummaryApp2()));
        caseDataBuilder.submitDocumentHearsayNoticeApp2(copyDraftDocumentToSubmitted(caseData.getDocumentHearsayNoticeApp2()));
        caseDataBuilder.submitDocumentReferredInStatementApp2(copyDraftDocumentToSubmitted(caseData.getDocumentReferredInStatementApp2()));
        caseDataBuilder.submitDocumentExpertReportApp2(copyDraftDocumentToSubmitted(caseData.getDocumentExpertReportApp2()));
        caseDataBuilder.submitDocumentJointStatementApp2(copyDraftDocumentToSubmitted(caseData.getDocumentJointStatementApp2()));
        caseDataBuilder.submitDocumentQuestionsApp2(copyDraftDocumentToSubmitted(caseData.getDocumentQuestionsApp2()));
        caseDataBuilder.submitDocumentAnswersApp2(copyDraftDocumentToSubmitted(caseData.getDocumentAnswersApp2()));
        caseDataBuilder.submitDocumentCaseSummaryApp2(copyDraftDocumentToSubmitted(caseData.getDocumentCaseSummaryApp2()));
        caseDataBuilder.submitDocumentSkeletonArgumentApp2(copyDraftDocumentToSubmitted(caseData.getDocumentSkeletonArgumentApp2()));
        caseDataBuilder.submitDocumentAuthoritiesApp2(copyDraftDocumentToSubmitted(caseData.getDocumentAuthoritiesApp2()));
        caseDataBuilder.submitDocumentCostsApp2(copyDraftDocumentToSubmitted(caseData.getDocumentCostsApp2()));
        caseDataBuilder.submitDocumentEvidenceForTrialApp2(copyDraftDocumentToSubmitted(caseData.getDocumentEvidenceForTrialApp2()));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    <T> List<Element<T>> copyDraftDocumentToSubmitted(List<Element<T>> draftDocumentToSubmit) {
        System.out.println("CALLED");

        return draftDocumentToSubmit;
    }

}

