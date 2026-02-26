package uk.gov.hmcts.reform.civil.handler.callback.user.dj.tasks.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskResult;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.STANDARD_DIRECTION_ORDER_DJ;

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
        DynamicList disposalList = dynamicListWithSingleValue("original disposal");
        DynamicList trialList = dynamicListWithSingleValue("original trial");
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setDisposalHearingMethodInPersonDJ(disposalList);
        caseData.setTrialHearingMethodInPersonDJ(trialList);
        var document = CaseDocumentBuilder.builder().documentName("dj-order.pdf").build();
        when(documentService.generateOrder(caseData, "auth-token")).thenReturn(Optional.of(document));
        DynamicList trimmedDisposal = dynamicListWithSingleValue("trimmed disposal");
        DynamicList trimmedTrial = dynamicListWithSingleValue("trimmed trial");
        when(locationService.trimListItems(disposalList)).thenReturn(trimmedDisposal);
        when(locationService.trimListItems(trialList)).thenReturn(trimmedTrial);

        CallbackParams params = new CallbackParams()
            .params(Map.of(BEARER_TOKEN, "auth-token"));
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
        CaseData caseData = CaseDataBuilder.builder().build();
        when(documentService.generateOrder(caseData, "token")).thenReturn(Optional.empty());

        CallbackParams params = new CallbackParams()
            .params(Map.of(BEARER_TOKEN, "token"));
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

    @Test
    void shouldOnlyApplyToStandardDirectionOrderEvent() {
        CaseData caseData = CaseDataBuilder.builder().build();
        CallbackParams matching = CallbackParamsBuilder.builder()
            .of(CallbackType.ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId(STANDARD_DIRECTION_ORDER_DJ.name()).build())
            .build();
        DirectionsOrderTaskContext matchingContext =
            new DirectionsOrderTaskContext(caseData, matching, DirectionsOrderLifecycleStage.DOCUMENT_GENERATION);

        assertThat(task.appliesTo(matchingContext)).isTrue();

        CallbackParams nonMatching = CallbackParamsBuilder.builder()
            .of(CallbackType.ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId("OTHER_EVENT").build())
            .build();
        DirectionsOrderTaskContext nonMatchingContext =
            new DirectionsOrderTaskContext(caseData, nonMatching, DirectionsOrderLifecycleStage.DOCUMENT_GENERATION);

        assertThat(task.appliesTo(nonMatchingContext)).isFalse();
    }

    private DynamicList dynamicListWithSingleValue(String label) {
        DynamicListElement element = DynamicListElement.dynamicElement(label);
        DynamicList list = new DynamicList();
        list.setValue(element);
        list.setListItems(List.of(element));
        return list;
    }
}
