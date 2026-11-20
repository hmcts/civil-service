package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicantonesolcitor.ApplicantOneWithoutPrejudiceDocumentHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.documents.DocumentWithDescription;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.APPLICANTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class ApplicantDocumentUploadTaskPart36Test {

    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private ApplicantOneWithoutPrejudiceDocumentHandler withoutPrejudiceHandler;

    private ApplicantDocumentUploadTask task;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        task = new ApplicantDocumentUploadTask(
            featureToggleService,
            objectMapper,
            List.of(withoutPrejudiceHandler),
            List.of()
        );
    }

    @Test
    void shouldSkipPart36HandlerWhenFlagIsOff() {
        when(featureToggleService.isPart36Enabled()).thenReturn(false);
        when(withoutPrejudiceHandler.getEvidenceUploadType()).thenReturn(EvidenceUploadType.WITHOUT_PREJUDICE);
        CaseData caseData = CaseData.builder().build();

        var response = (AboutToStartOrSubmitCallbackResponse) task.uploadDocuments(
            caseData, CaseData.builder().build(), APPLICANTSOLICITORONE.name());

        verify(withoutPrejudiceHandler, never()).handleDocuments(any(), any(), any());
        assertThat(response.getData()).doesNotContainKeys(
            "documentPart36Rejection",
            "documentPart36RejectionApp2",
            "documentPart36RejectionRes",
            "documentPart36RejectionRes2",
            "withoutPrejudiceSelectionEvidence",
            "withoutPrejudiceSelectionEvidenceSmallClaim",
            "withoutPrejudiceSelectionEvidenceSmallClaimRes"
        );
    }

    @Test
    void shouldHandlePart36DocumentsWhenFlagIsOn() {
        when(featureToggleService.isPart36Enabled()).thenReturn(true);
        CaseData caseData = CaseData.builder()
            .documentPart36Rejection(List.of(element(new DocumentWithDescription()
                                                         .setDocumentDescription("submitted"))))
            .build();

        var response = (AboutToStartOrSubmitCallbackResponse) task.uploadDocuments(
            caseData, CaseData.builder().build(), APPLICANTSOLICITORONE.name());

        verify(withoutPrejudiceHandler).handleDocuments(any(), any(), any());
        assertThat(response.getData()).containsKeys(
            "withoutPrejudiceSelectionEvidence",
            "withoutPrejudiceSelectionEvidenceSmallClaim",
            "withoutPrejudiceSelectionEvidenceSmallClaimRes"
        );
        assertThat(response.getData().get("withoutPrejudiceSelectionEvidence")).isNull();
    }
}
