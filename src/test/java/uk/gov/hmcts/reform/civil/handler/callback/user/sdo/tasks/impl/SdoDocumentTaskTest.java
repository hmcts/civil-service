package uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskResult;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.sdo.SdoDocumentService;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

@ExtendWith(MockitoExtension.class)
class SdoDocumentTaskTest {

    private static final String AUTH_TOKEN = "auth-token";

    @Mock
    private SdoDocumentService sdoDocumentService;

    @Test
    void shouldGenerateAndAssignDocument() {
        SdoDocumentTask task = new SdoDocumentTask(sdoDocumentService);
        CaseData caseData = CaseDataBuilder.builder().build();
        CaseDocument document = new CaseDocument();
        CallbackParams params = CallbackParams.builder()
            .params(Map.of(BEARER_TOKEN, AUTH_TOKEN))
            .type(CallbackType.MID)
            .build();
        DirectionsOrderTaskContext context =
            new DirectionsOrderTaskContext(caseData, params, DirectionsOrderLifecycleStage.DOCUMENT_GENERATION);

        when(sdoDocumentService.generateSdoDocument(caseData, AUTH_TOKEN)).thenReturn(Optional.of(document));

        DirectionsOrderTaskResult result = task.execute(context);

        assertThat(result.updatedCaseData()).isEqualTo(caseData);
        assertThat(result.errors()).isEmpty();
        assertThat(result.updatedCaseData().getSdoOrderDocument()).isEqualTo(document);
        verify(sdoDocumentService).generateSdoDocument(caseData, AUTH_TOKEN);
        verify(sdoDocumentService).assignCategory(document, "caseManagementOrders");
    }

    @Test
    void shouldSkipCategoryAssignmentWhenDocumentMissing() {
        SdoDocumentTask task = new SdoDocumentTask(sdoDocumentService);
        CaseData caseData = CaseDataBuilder.builder().build();
        CallbackParams params = CallbackParams.builder()
            .params(Map.of(BEARER_TOKEN, AUTH_TOKEN))
            .build();
        DirectionsOrderTaskContext context =
            new DirectionsOrderTaskContext(caseData, params, DirectionsOrderLifecycleStage.DOCUMENT_GENERATION);

        when(sdoDocumentService.generateSdoDocument(caseData, AUTH_TOKEN)).thenReturn(Optional.empty());

        DirectionsOrderTaskResult result = task.execute(context);

        verify(sdoDocumentService).generateSdoDocument(caseData, AUTH_TOKEN);
        verify(sdoDocumentService, never()).assignCategory(any(), anyString());
        assertThat(result.updatedCaseData()).isEqualTo(caseData);
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void shouldSupportDocumentGenerationStageOnly() {
        SdoDocumentTask task = new SdoDocumentTask(sdoDocumentService);

        assertThat(task.supports(DirectionsOrderLifecycleStage.DOCUMENT_GENERATION)).isTrue();
        assertThat(task.supports(DirectionsOrderLifecycleStage.PRE_POPULATE)).isFalse();
    }
}
