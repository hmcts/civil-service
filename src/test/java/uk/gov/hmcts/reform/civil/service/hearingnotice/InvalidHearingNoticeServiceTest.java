package uk.gov.hmcts.reform.civil.service.hearingnotice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.InvalidHearingNoticeProcessed;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INVALID_HEARING_NOTICE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class InvalidHearingNoticeServiceTest {

    private static final String CASE_ID = "1234";
    private static final String FIELD = "invalidHearingNoticeProcessed";
    private static final LocalDateTime RECEIVED = LocalDateTime.of(2026, 9, 28, 10, 0);
    private static final InvalidHearingNoticeProcessed RESPONSE = new InvalidHearingNoticeProcessed("hearing-1", 1L, RECEIVED);
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private CoreCaseDataService coreCaseDataService;
    private InvalidHearingNoticeService service;

    @BeforeEach
    void setUp() {
        service = new InvalidHearingNoticeService(coreCaseDataService, mapper);
    }

    @Test
    void shouldRoundTripCcdCollectionAndMatchOnlyRecordedResponse() throws Exception {
        CaseData data = CaseData.builder().invalidHearingNoticeProcessed(List.of(element(RESPONSE))).build();
        CaseData restored = mapper.readValue(mapper.writeValueAsString(data), CaseData.class);
        assertThat(restored.getInvalidHearingNoticeProcessed()).isEqualTo(data.getInvalidHearingNoticeProcessed());
        assertThat(service.hasProcessed(CaseDetails.builder().data(restored.toMap(mapper)).build(), RESPONSE)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("differentResponses")
    void shouldNotSuppressAnotherHearingOrUpdatedOrIncompleteResponse(InvalidHearingNoticeProcessed response) {
        assertThat(service.hasProcessed(details(List.of(element(RESPONSE))), response)).isFalse();
    }

    static Stream<InvalidHearingNoticeProcessed> differentResponses() {
        return Stream.of(
            new InvalidHearingNoticeProcessed("hearing-2", 1L, RECEIVED),
            new InvalidHearingNoticeProcessed("hearing-1", 2L, RECEIVED),
            new InvalidHearingNoticeProcessed("hearing-1", 1L, RECEIVED.plusDays(1)),
            new InvalidHearingNoticeProcessed(null, 1L, RECEIVED),
            new InvalidHearingNoticeProcessed(" ", 1L, RECEIVED),
            new InvalidHearingNoticeProcessed("hearing-1", null, RECEIVED),
            new InvalidHearingNoticeProcessed("hearing-1", 1L, null));
    }

    @Test
    void shouldNotSuppressCasesWithoutRecords() {
        assertThat(service.hasProcessed(CaseDetails.builder().data(Map.of()).build(), RESPONSE)).isFalse();
        assertThat(service.hasProcessed(CaseDetails.builder().build(), RESPONSE)).isFalse();
        assertThat(service.hasProcessed(details(List.of()), RESPONSE)).isFalse();
    }

    @Test
    void shouldCommitRecordAndEventTogetherPreservingOtherHearingsAndCaseData() {
        Element<InvalidHearingNoticeProcessed> other = element(new InvalidHearingNoticeProcessed("other", 1L, RECEIVED));
        when(coreCaseDataService.startUpdate(CASE_ID, INVALID_HEARING_NOTICE)).thenReturn(start(List.of(other)));
        stubContentBuilder();

        service.recordAndTriggerEvent(CASE_ID, RESPONSE, "process-1");

        var captor = ArgumentCaptor.forClass(CaseDataContent.class);
        verify(coreCaseDataService).submitUpdate(eq(CASE_ID), captor.capture());
        var content = captor.getValue();
        assertThat(content.getEventToken()).isEqualTo("token");
        assertThat(content.getEvent().getId()).isEqualTo(INVALID_HEARING_NOTICE.name());
        assertThat(content.getEvent().getSummary()).isEqualTo("Invalid hearing notice");
        assertThat(content.getEvent().getDescription()).isEqualTo("process-1");
        Map<String, Object> payload = mapper.convertValue(content.getData(), new TypeReference<>() {});
        assertThat(payload).containsEntry("unrelatedField", "preserved");
        var data = mapper.convertValue(Map.of(FIELD, payload.get(FIELD)), CaseData.class);
        assertThat(data.getInvalidHearingNoticeProcessed()).hasSize(2);
        assertThat(data.getInvalidHearingNoticeProcessed().getFirst()).isEqualTo(other);
        assertThat(data.getInvalidHearingNoticeProcessed().getLast().getValue()).isEqualTo(RESPONSE);
    }

    @Test
    void shouldNotRepeatEventForAnotherProcessHandlingTheSameResponse() {
        when(coreCaseDataService.startUpdate(CASE_ID, INVALID_HEARING_NOTICE))
            .thenReturn(start(List.of(element(RESPONSE))));

        service.recordAndTriggerEvent(CASE_ID, RESPONSE, "another-process");

        verify(coreCaseDataService, never()).submitUpdate(any(), any());
    }

    @Test
    void shouldRetryAfterFailedSubmissionWithoutPersistingSuppression() {
        when(coreCaseDataService.startUpdate(CASE_ID, INVALID_HEARING_NOTICE)).thenReturn(start(List.of()));
        stubContentBuilder();
        when(coreCaseDataService.submitUpdate(eq(CASE_ID), any()))
            .thenThrow(new IllegalStateException("CCD failed")).thenReturn(CaseData.builder().build());

        assertThatThrownBy(() -> service.recordAndTriggerEvent(CASE_ID, RESPONSE, "process"))
            .hasMessage("CCD failed");
        service.recordAndTriggerEvent(CASE_ID, RESPONSE, "process");

        verify(coreCaseDataService, times(2)).startUpdate(CASE_ID, INVALID_HEARING_NOTICE);
        verify(coreCaseDataService, times(2)).submitUpdate(eq(CASE_ID), any());
    }

    @Test
    void shouldRecogniseCommittedRecordAfterLostResponseOrCamundaFailure() {
        when(coreCaseDataService.startUpdate(CASE_ID, INVALID_HEARING_NOTICE))
            .thenReturn(start(List.of()), start(List.of(element(RESPONSE))));
        stubContentBuilder();
        when(coreCaseDataService.submitUpdate(eq(CASE_ID), any()))
            .thenThrow(new IllegalStateException("Response lost after commit"));

        assertThatThrownBy(() -> service.recordAndTriggerEvent(CASE_ID, RESPONSE, "process"))
            .hasMessage("Response lost after commit");
        service.recordAndTriggerEvent(CASE_ID, RESPONSE, "retry-process");

        verify(coreCaseDataService).submitUpdate(eq(CASE_ID), any());
    }

    @Test
    void shouldRecheckRecordAfterCcdConflict() {
        when(coreCaseDataService.startUpdate(CASE_ID, INVALID_HEARING_NOTICE))
            .thenReturn(start(List.of()), start(List.of(element(RESPONSE))));
        stubContentBuilder();
        when(coreCaseDataService.submitUpdate(eq(CASE_ID), any())).thenThrow(conflict());

        service.recordAndTriggerEvent(CASE_ID, RESPONSE, "process");

        verify(coreCaseDataService, times(2)).startUpdate(CASE_ID, INVALID_HEARING_NOTICE);
        verify(coreCaseDataService).submitUpdate(eq(CASE_ID), any());
    }

    @Test
    void shouldMergeNewlyAddedOtherHearingAfterConflict() {
        var other = element(new InvalidHearingNoticeProcessed("other", 1L, RECEIVED));
        when(coreCaseDataService.startUpdate(CASE_ID, INVALID_HEARING_NOTICE))
            .thenReturn(start(List.of()), start(List.of(other)));
        stubContentBuilder();
        when(coreCaseDataService.submitUpdate(eq(CASE_ID), any()))
            .thenThrow(conflict()).thenReturn(CaseData.builder().build());

        service.recordAndTriggerEvent(CASE_ID, RESPONSE, "process");

        var captor = ArgumentCaptor.forClass(CaseDataContent.class);
        verify(coreCaseDataService, times(2)).submitUpdate(eq(CASE_ID), captor.capture());
        Map<?, ?> payload = (Map<?, ?>) captor.getValue().getData();
        var data = mapper.convertValue(Map.of(FIELD, payload.get(FIELD)), CaseData.class);
        assertThat(data.getInvalidHearingNoticeProcessed()).hasSize(2).contains(other);
    }

    @Test
    void shouldPropagatePersistentConflictAfterBoundedRetries() {
        when(coreCaseDataService.startUpdate(CASE_ID, INVALID_HEARING_NOTICE)).thenReturn(start(List.of()));
        stubContentBuilder();
        when(coreCaseDataService.submitUpdate(eq(CASE_ID), any())).thenThrow(conflict());

        assertThatThrownBy(() -> service.recordAndTriggerEvent(CASE_ID, RESPONSE, "process"))
            .isInstanceOf(FeignException.Conflict.class);

        verify(coreCaseDataService, times(3)).submitUpdate(eq(CASE_ID), any());
    }

    private void stubContentBuilder() {
        when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), any())).thenCallRealMethod();
    }

    private StartEventResponse start(List<Element<InvalidHearingNoticeProcessed>> records) {
        return StartEventResponse.builder().token("token").eventId(INVALID_HEARING_NOTICE.name())
            .caseDetails(details(records)).build();
    }

    private CaseDetails details(List<Element<InvalidHearingNoticeProcessed>> records) {
        return CaseDetails.builder().data(Map.of(FIELD, records, "unrelatedField", "preserved")).build();
    }

    private FeignException.Conflict conflict() {
        return new FeignException.Conflict("conflict", Request.create(Request.HttpMethod.POST, "/cases",
            Map.of(), (byte[]) null, java.nio.charset.StandardCharsets.UTF_8, null), null, Map.of());
    }
}
