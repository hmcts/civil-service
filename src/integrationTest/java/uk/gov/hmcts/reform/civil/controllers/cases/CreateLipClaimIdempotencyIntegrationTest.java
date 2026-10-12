package uk.gov.hmcts.reform.civil.controllers.cases;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.EventDto;
import uk.gov.hmcts.reform.draftstore.DraftType;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;
import uk.gov.hmcts.reform.draftstore.repositories.DraftStoreRepository;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Execution(ExecutionMode.SAME_THREAD)
class CreateLipClaimIdempotencyIntegrationTest extends BaseIntegrationTest {

    private static final String SUBMIT_EVENT_URL = "/cases/{caseId}/citizen/{submitterId}/event";
    private static final String USER_ID = "lip-user-1";
    private static final String SERVICE_TOKEN = "service-token";
    private static final long CREATED_CASE_ID = 99_001L;

    @Autowired
    private DraftStoreRepository draftStoreRepository;

    @MockitoBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockitoBean
    private AuthTokenGenerator authTokenGenerator;

    private UUID draftId;
    private String submissionId;

    @BeforeEach
    void setUp() {
        draftStoreRepository.deleteAll();
        draftId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-10-12T10:00:00Z");
        submissionId = USER_ID + ":" + draftId + ":" + createdAt;

        given(userService.getUserInfo(anyString()))
            .willReturn(UserInfo.builder().uid(USER_ID).sub("lip@test.com").build());
        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);

        CaseDetails createdCase = CaseDetails.builder()
            .id(CREATED_CASE_ID)
            .data(Map.of())
            .build();
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(CaseEvent.CREATE_LIP_CLAIM.name())
            .token("event-token")
            .caseDetails(CaseDetails.builder().id(null).data(new HashMap<>()).build())
            .build();

        given(coreCaseDataApi.startForCitizen(any(), any(), any(), any(), any(), any()))
            .willReturn(startEventResponse);
        given(coreCaseDataApi.submitForCitizen(any(), any(), any(), any(), any(), anyBoolean(), any()))
            .willReturn(createdCase);
        given(coreCaseDataApi.getCase(any(), any(), any()))
            .willReturn(createdCase);

        DraftStoreEntity draft = new DraftStoreEntity(
            draftId,
            USER_ID,
            null,
            DraftType.DRAFT_CLAIM,
            Map.of("step", "ready-to-submit"),
            createdAt,
            createdAt,
            createdAt.plusDays(30),
            null
        );
        draftStoreRepository.saveAndFlush(draft);
    }

    @AfterEach
    void tearDown() {
        draftStoreRepository.deleteAll();
    }

    @Test
    void shouldCreateCaseOnceAndLinkDraftOnFirstSubmit() throws Exception {
        doPost(BEARER_TOKEN, createLipClaimEvent(), SUBMIT_EVENT_URL, "draft", USER_ID)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(CREATED_CASE_ID));

        DraftStoreEntity draft = draftStoreRepository.findById(draftId).orElseThrow();
        assertThat(draft.getSubmissionId()).isEqualTo(submissionId);
        assertThat(draft.getCaseId()).isEqualTo(String.valueOf(CREATED_CASE_ID));
        verify(coreCaseDataApi, times(1)).submitForCitizen(any(), any(), any(), any(), any(), anyBoolean(), any());
    }

    @Test
    void shouldReturnExistingCaseOnRetryWithSameSubmissionIdWithoutCreatingAgain() throws Exception {
        doPost(BEARER_TOKEN, createLipClaimEvent(), SUBMIT_EVENT_URL, "draft", USER_ID)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(CREATED_CASE_ID));

        doPost(BEARER_TOKEN, createLipClaimEvent(), SUBMIT_EVENT_URL, "draft", USER_ID)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(CREATED_CASE_ID));

        verify(coreCaseDataApi, times(1)).submitForCitizen(any(), any(), any(), any(), any(), anyBoolean(), any());
        DraftStoreEntity draft = draftStoreRepository.findById(draftId).orElseThrow();
        assertThat(draft.getCaseId()).isEqualTo(String.valueOf(CREATED_CASE_ID));
        assertThat(draft.getSubmissionId()).isEqualTo(submissionId);
    }

    @Test
    void shouldReturnConflictWhenSecondSubmitArrivesWhileFirstIsStillCreatingCcdCase() throws Exception {
        CountDownLatch enteredCcd = new CountDownLatch(1);
        CountDownLatch releaseCcd = new CountDownLatch(1);
        AtomicInteger ccdCreates = new AtomicInteger();

        given(coreCaseDataApi.submitForCitizen(any(), any(), any(), any(), any(), anyBoolean(), any()))
            .willAnswer(invocation -> {
                ccdCreates.incrementAndGet();
                enteredCcd.countDown();
                if (!releaseCcd.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Timed out waiting to release CCD create");
                }
                return CaseDetails.builder().id(CREATED_CASE_ID).data(Map.of()).build();
            });

        List<Integer> statuses = Collections.synchronizedList(new ArrayList<>());
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            final Future<?> first = executor.submit(() -> {
                try {
                    MvcResult result = doPost(
                        BEARER_TOKEN,
                        createLipClaimEvent(),
                        SUBMIT_EVENT_URL,
                        "draft",
                        USER_ID
                    ).andReturn();
                    statuses.add(result.getResponse().getStatus());
                } catch (Exception ex) {
                    throw new IllegalStateException(ex);
                }
            });

            assertThat(enteredCcd.await(10, TimeUnit.SECONDS)).isTrue();

            final Future<?> second = executor.submit(() -> {
                try {
                    MvcResult result = doPost(
                        BEARER_TOKEN,
                        createLipClaimEvent(),
                        SUBMIT_EVENT_URL,
                        "draft",
                        USER_ID
                    ).andReturn();
                    statuses.add(result.getResponse().getStatus());
                } catch (Exception ex) {
                    throw new IllegalStateException(ex);
                }
            });

            second.get(10, TimeUnit.SECONDS);
            releaseCcd.countDown();
            first.get(10, TimeUnit.SECONDS);
        }

        assertThat(statuses).containsExactlyInAnyOrder(200, 409);
        assertThat(ccdCreates.get()).isEqualTo(1);

        DraftStoreEntity draft = draftStoreRepository.findById(draftId).orElseThrow();
        assertThat(draft.getSubmissionId()).isEqualTo(submissionId);
        assertThat(draft.getCaseId()).isEqualTo(String.valueOf(CREATED_CASE_ID));
    }

    @Test
    void shouldCreateOnlyOneCcdCaseWhenTwoSubmitsRace() throws Exception {
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        List<Integer> statuses = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger ccdCreates = new AtomicInteger();

        given(coreCaseDataApi.submitForCitizen(any(), any(), any(), any(), any(), anyBoolean(), any()))
            .willAnswer(invocation -> {
                ccdCreates.incrementAndGet();
                Thread.sleep(200);
                return CaseDetails.builder().id(CREATED_CASE_ID).data(Map.of()).build();
            });

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Runnable submit = () -> {
                try {
                    ready.countDown();
                    start.await();
                    MvcResult result = doPost(
                        BEARER_TOKEN,
                        createLipClaimEvent(),
                        SUBMIT_EVENT_URL,
                        "draft",
                        USER_ID
                    ).andReturn();
                    statuses.add(result.getResponse().getStatus());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(ex);
                } catch (Exception ex) {
                    throw new IllegalStateException(ex);
                }
            };

            final Future<?> first = executor.submit(submit);
            final Future<?> second = executor.submit(submit);
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            first.get(15, TimeUnit.SECONDS);
            second.get(15, TimeUnit.SECONDS);
        }

        assertThat(statuses).contains(200);
        assertThat(statuses).allMatch(status -> status == 200 || status == 409);
        assertThat(ccdCreates.get()).isEqualTo(1);

        DraftStoreEntity draft = draftStoreRepository.findById(draftId).orElseThrow();
        assertThat(draft.getCaseId()).isEqualTo(String.valueOf(CREATED_CASE_ID));
        assertThat(draft.getSubmissionId()).isEqualTo(submissionId);
    }

    private EventDto createLipClaimEvent() {
        return new EventDto()
            .setEvent(CaseEvent.CREATE_LIP_CLAIM)
            .setCaseDataUpdate(Map.of())
            .setSubmissionId(submissionId);
    }
}
