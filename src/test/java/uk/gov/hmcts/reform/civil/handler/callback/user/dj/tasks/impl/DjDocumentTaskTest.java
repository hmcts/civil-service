package uk.gov.hmcts.reform.civil.handler.callback.user.dj.tasks.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskResult;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.dj.DjDocumentService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoLocationService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

@ExtendWith(MockitoExtension.class)
class DjDocumentTaskTest {

    @Mock
    private DjDocumentService documentService;
    @Mock
    private SdoLocationService locationService;
    @InjectMocks
    private DjDocumentTask task;

    @Test
    void shouldAppendGeneratedDocumentAndTrimLists() {
        DynamicList disposalList = DynamicList.builder()
            .value(DynamicListElement.dynamicElement("original disposal"))
            .listItems(List.of(DynamicListElement.dynamicElement("original disposal")))
            .build();
        DynamicList trialList = DynamicList.builder()
            .value(DynamicListElement.dynamicElement("original trial"))
            .listItems(List.of(DynamicListElement.dynamicElement("original trial")))
            .build();
        DynamicList trimmedDisposal = DynamicList.builder()
            .value(DynamicListElement.dynamicElement("trimmed disposal"))
            .listItems(List.of(DynamicListElement.dynamicElement("trimmed disposal")))
            .build();
        DynamicList trimmedTrial = DynamicList.builder()
            .value(DynamicListElement.dynamicElement("trimmed trial"))
            .listItems(List.of(DynamicListElement.dynamicElement("trimmed trial")))
            .build();
        CaseData caseData = CaseData.builder()
            .disposalHearingMethodInPersonDJ(disposalList)
            .trialHearingMethodInPersonDJ(trialList)
            .build();
        var document = CaseDocumentBuilder.builder().documentName("dj-order.pdf").build();
        when(documentService.generateOrder(caseData, "auth-token")).thenReturn(Optional.of(document));
        when(locationService.trimListItems(disposalList)).thenReturn(trimmedDisposal);
        when(locationService.trimListItems(trialList)).thenReturn(trimmedTrial);

        CallbackParams params = CallbackParams.builder()
            .params(Map.of(BEARER_TOKEN, "auth-token"))
            .build();
        DirectionsOrderTaskContext context =
            new DirectionsOrderTaskContext(caseData, params, DirectionsOrderLifecycleStage.DOCUMENT_GENERATION);

        DirectionsOrderTaskResult result = task.execute(context);

        assertThat(result.updatedCaseData().getOrderSDODocumentDJ()).isEqualTo(document.getDocumentLink());
        assertThat(result.updatedCaseData().getOrderSDODocumentDJCollection())
            .hasSize(1)
            .first()
            .extracting(element -> element.getValue().getDocumentLink())
            .isEqualTo(document.getDocumentLink());
        assertThat(result.updatedCaseData().getDisposalHearingMethodInPersonDJ()).isEqualTo(trimmedDisposal);
        assertThat(result.updatedCaseData().getTrialHearingMethodInPersonDJ()).isEqualTo(trimmedTrial);
        verify(documentService).assignCategory(document, "caseManagementOrders");
    }

    @Test
    void shouldReturnOriginalWhenDocumentNotGenerated() {
        CaseData caseData = CaseData.builder().build();
        when(documentService.generateOrder(caseData, "token")).thenReturn(Optional.empty());

        CallbackParams params = CallbackParams.builder()
            .params(Map.of(BEARER_TOKEN, "token"))
            .build();
        DirectionsOrderTaskContext context =
            new DirectionsOrderTaskContext(caseData, params, DirectionsOrderLifecycleStage.DOCUMENT_GENERATION);

        DirectionsOrderTaskResult result = task.execute(context);

        assertThat(result.updatedCaseData()).isEqualTo(caseData);
        assertThat(result.errors()).isEmpty();
        verify(documentService, never()).assignCategory(any(), any());
        verify(locationService, never()).trimListItems(any());
    }

    @Test
    void shouldSupportDocumentGenerationStageOnly() {
        assertThat(task.supports(DirectionsOrderLifecycleStage.DOCUMENT_GENERATION)).isTrue();
        assertThat(task.supports(DirectionsOrderLifecycleStage.ORDER_DETAILS)).isFalse();
    }
}
