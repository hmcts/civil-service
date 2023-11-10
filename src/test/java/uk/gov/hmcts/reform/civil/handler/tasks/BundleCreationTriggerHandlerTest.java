package uk.gov.hmcts.reform.civil.handler.tasks;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.BundleCreationTriggerEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.search.BundleCreationTriggerService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class BundleCreationTriggerHandlerTest {

    @Mock
    private ExternalTask mockTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @Mock
    private BundleCreationTriggerService searchService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private BundleCreationTriggerHandler handler;
    private CaseData caseData;
    private CaseDetails caseDetails;
    private List<IdValue<Bundle>> caseBundles;

    @BeforeEach
    void init() {
        when(mockTask.getTopicName()).thenReturn("test");
        when(mockTask.getWorkerId()).thenReturn("worker");

        caseBundles = new ArrayList<>();
        caseBundles.add(new IdValue<>("1", uk.gov.hmcts.reform.civil.model.Bundle.builder().id("1")
            .title("Trial Bundle")
            .stitchStatus(Optional.of("NEW")).description("Trial Bundle")
            .build()));
        caseData = CaseData.builder().caseBundles(caseBundles).build();
    }

    @Test
    void shouldEmitBundleCreationEvent_whenCasesFound() {
        long caseId = 1L;
        CaseData caseData = CaseDataBuilder.builder().atStateHearingDateScheduled().build();
        Map<String, Object> data = Map.of("data", caseData);
        List<CaseDetails> caseDetails = List.of(CaseDetails.builder().id(caseId).data(data).build());

        when(searchService.getCases()).thenReturn(caseDetails);
        when(coreCaseDataService.getCase(caseId)).thenReturn(caseDetails.get(0));
        when(caseDetailsConverter.toCaseData(caseDetails.get(0))).thenReturn(caseData);
        when(coreCaseDataService.getCase(anyLong())).thenReturn(caseDetails.get(0));
        when(caseDetailsConverter.toCaseData(anyMap())).thenReturn(caseData);

        handler.execute(mockTask, externalTaskService);

        verify(applicationEventPublisher).publishEvent(new BundleCreationTriggerEvent(caseId));
        verify(externalTaskService).complete(mockTask);
    }

    @Test
    void shouldNotEmitBundleCreationEvent_WhenNoCasesFound() {
        when(searchService.getCases()).thenReturn(List.of());

        handler.execute(mockTask, externalTaskService);

        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldCallHandleFailureMethod_whenExceptionFromBusinessLogic() {
        String errorMessage = "there was an error";

        when(mockTask.getRetries()).thenReturn(null);
        when(searchService.getCases()).thenAnswer(invocation -> {
            throw new Exception(errorMessage);
        });

        handler.execute(mockTask, externalTaskService);

        verify(externalTaskService, never()).complete(mockTask);
        verify(externalTaskService).handleFailure(
            eq(mockTask),
            eq(errorMessage),
            anyString(),
            eq(2),
            eq(1000L)
        );
    }

    @Test
    void shouldNotCallHandleFailureMethod_whenExceptionOnCompleteCall() {
        handler.execute(mockTask, externalTaskService);

        verify(externalTaskService, never()).handleFailure(
            any(ExternalTask.class),
            anyString(),
            anyString(),
            anyInt(),
            anyLong()
        );
    }

    @Test
    void shouldHandleExceptionAndContinue_whenOneCaseErrors() {
        long caseId = 1L;
        long otherId = 2L;
        Map<String, Object> data = Map.of("data", "some data");
        List<CaseDetails> caseDetails = List.of(
            CaseDetails.builder().id(caseId).data(data).build(),
            CaseDetails.builder().id(otherId).data(data).build());

        when(searchService.getCases()).thenReturn(caseDetails);

        String errorMessage = "there was an error";

        handler.execute(mockTask, externalTaskService);

        verify(externalTaskService, never()).handleFailure(
            any(ExternalTask.class),
            anyString(),
            anyString(),
            anyInt(),
            anyLong()
        );
    }

    @Test
    void shouldReturnFalseWhenBundleHearingDateIsNotEqualToHearingDate() {
        //Given: caseData with hearing date different from caseBundles hearing date
        caseData.setHearingDate(LocalDate.of(2023, 10, 12));
        caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        when(coreCaseDataService.getCase(1L)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(anyMap())).thenReturn(caseData);
        //When: getIsBundleCreatedForHearingDate is called
        //Then: its should return false indicating that bundle is not already created for this hearingDate
        Assertions.assertEquals(handler.getIsBundleCreatedForHearingDate(1L), false);
    }

    @Test
    void shouldReturnFalseWhenBundleHearingDateIsNull() {
        //Given: caseBundles with bundle hearing date null
        List<IdValue<Bundle>> caseBundles = new ArrayList<>();
        caseBundles.add(new IdValue<>("1", uk.gov.hmcts.reform.civil.model.Bundle.builder().id("1")
            .title("Trial Bundle")
            .stitchStatus(Optional.of("NEW")).description("Trial Bundle")
            .build()));

        caseData = CaseData.builder().caseBundles(caseBundles).hearingDate(LocalDate.now()).build();
        when(coreCaseDataService.getCase(1L)).thenReturn(caseDetails);
        caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        when(coreCaseDataService.getCase(anyLong())).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(anyMap())).thenReturn(caseData);
        //When: getIsBundleCreatedForHearingDate is called
        //Then: its should return false indicating that bundle is not already created for this hearingDate
        Assertions.assertEquals(false, handler.getIsBundleCreatedForHearingDate(1L));
    }

    @Test
    void shouldReturnFalseWhenAnyBundleHearingDateIsNull() {
        //Given: caseBundles with bundle hearing date null
        caseData = CaseData.builder().caseBundles(caseBundles).hearingDate(LocalDate.now()).build();
        caseData.setHearingDate(LocalDate.of(2023, 12, 12));
        List<IdValue<uk.gov.hmcts.reform.civil.model.Bundle>> caseBundles = new ArrayList<>();
        caseBundles.add(new IdValue<>("1", uk.gov.hmcts.reform.civil.model.Bundle.builder().id("1")
            .title("Trial Bundle")
            .stitchStatus(Optional.of("NEW")).description("Trial Bundle")
            .build()));
        caseBundles.add(new IdValue<>("2", uk.gov.hmcts.reform.civil.model.Bundle.builder().id("1")
            .title("Trial Bundle")
            .stitchStatus(Optional.of("NEW")).description("Trial Bundle")
            .createdOn(Optional.of(LocalDateTime.now()))
            .bundleHearingDate(Optional.of(LocalDate.of(2023, 12, 12)))
            .build()));
        caseData = CaseData.builder().caseBundles(caseBundles).hearingDate(LocalDate.of(2023, 12, 12)).build();
        caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        when(coreCaseDataService.getCase(1L)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(anyMap())).thenReturn(caseData);
        //When: getIsBundleCreatedForHearingDate is called
        //Then: its should return false indicating that bundle is not already created for this hearingDate
        Assertions.assertEquals(true, handler.getIsBundleCreatedForHearingDate(1L));
    }

    @Test
    void shouldReturnTrueWhenBundleHearingDateIsEqualToHearingDate() {
        caseBundles.add(new IdValue<>("2", uk.gov.hmcts.reform.civil.model.Bundle.builder().id("1")
            .title("Trial Bundle")
            .stitchStatus(Optional.of("NEW")).description("Trial Bundle")
            .createdOn(Optional.of(LocalDateTime.now()))
            .bundleHearingDate(Optional.of(LocalDate.of(2023, 12, 12)))
            .build()));
        //Given : caseData with hearing date same as caseBundles hearing date
        caseData.setHearingDate(LocalDate.of(2023, 12, 12));
        caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        when(coreCaseDataService.getCase(1L)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(anyMap())).thenReturn(caseData);
        //When: getIsBundleCreatedForHearingDate is called
        //Then: its should return true indicating that bundle is already created for this hearingDate
        Assertions.assertEquals(true, handler.getIsBundleCreatedForHearingDate(1L));
    }
}

