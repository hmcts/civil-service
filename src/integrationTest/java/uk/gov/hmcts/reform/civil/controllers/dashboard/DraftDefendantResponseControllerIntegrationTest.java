package uk.gov.hmcts.reform.civil.controllers.dashboard;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.DraftDefendantResponseRequest;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftDefendantResponseAlreadyExistsException;
import uk.gov.hmcts.reform.dashboard.services.DraftDefendantResponseService;
import uk.gov.hmcts.reform.draftstore.DraftType;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;
import uk.gov.hmcts.reform.draftstore.repositories.DraftStoreRepository;
import uk.gov.hmcts.reform.draftstore.services.DraftStoreService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Execution(ExecutionMode.SAME_THREAD)
public class DraftDefendantResponseControllerIntegrationTest extends BaseIntegrationTest {

    private static final String DRAFT_RESPONSES_URL = "/dashboard/draft-responses";
    private static final String DRAFT_RESPONSE_BY_ID_URL = "/dashboard/draft-responses/{draft-id}";
    private static final String DRAFT_RESPONSE_BY_CASE_URL = "/dashboard/draft-responses/case/{case-id}";
    private static final String USER_ID = "user1";
    private static final String CASE_ID = "12345";
    private static final String OTHER_CASE_ID = "67890";
    private static final Map<String, Object> PAYLOAD = Map.of("step", "response-details");
    private static final DraftType DRAFT_TYPE = DraftType.DRAFT_DEFENDANT_RESPONSE;
    private static final long RETENTION_DAYS = DRAFT_TYPE.getRetentionDays();

    @Autowired
    private DraftStoreRepository draftStoreRepository;

    @Autowired
    private DraftDefendantResponseService draftDefendantResponseService;

    private UUID draftId;

    @BeforeEach
    void setUp() {
        draftStoreRepository.deleteAll();
        draftId = UUID.randomUUID();

        given(userService.getUserInfo(anyString()))
            .willReturn(
                UserInfo.builder()
                    .uid(USER_ID)
                    .sub("test@test.com")
                    .build()
            );

        OffsetDateTime now = OffsetDateTime.now();
        draftStoreRepository.save(draftResponse(
            draftId,
            now,
            now.plusDays(RETENTION_DAYS),
            "active-test"
        ));
    }

    @AfterEach
    void tearDown() {
        draftStoreRepository.deleteAll();
    }

    @Test
    void shouldCreateAndPersistDraftWhenNoneExistsForCase() throws Exception {
        draftStoreRepository.deleteAll();
        DraftDefendantResponseRequest request = new DraftDefendantResponseRequest(CASE_ID, PAYLOAD);

        MvcResult result = doPost(BEARER_TOKEN, request, DRAFT_RESPONSES_URL)
            .andExpect(status().isCreated())
            .andExpectAll(
                jsonPath("$.draftId").exists(),
                jsonPath("$.caseId").value(CASE_ID),
                jsonPath("$.payload.step").value("response-details")
            )
            .andReturn();

        UUID createdDraftId = UUID.fromString(JsonPath.read(result.getResponse().getContentAsString(), "$.draftId"));
        DraftStoreEntity draftInDb = draftStoreRepository.findById(createdDraftId)
            .orElseThrow(() -> new AssertionError("Draft defendant response should be persisted in database"));

        assertThat(draftInDb.getDraftType()).isEqualTo(DRAFT_TYPE);
        assertThat(draftInDb.getPayload()).extracting("step").isEqualTo("response-details");
        assertThat(draftInDb.getUserId()).isEqualTo(USER_ID);
        assertThat(draftInDb.getCaseId()).isEqualTo(CASE_ID);
        assertThat(draftInDb.getExpiresAt()).isEqualTo(
            DraftStoreService.calculateExpiresAt(draftInDb.getCreatedAt(), RETENTION_DAYS)
        );
    }

    @Test
    void shouldReturnConflictAndLeaveDraftUnchangedWhenResponseAlreadyExistsForCase() throws Exception {
        DraftStoreEntity originalDraft = draftStoreRepository.findById(draftId)
            .orElseThrow(() -> new AssertionError("Draft defendant response should exist in DB"));
        OffsetDateTime originalCreatedAt = originalDraft.getCreatedAt();
        OffsetDateTime originalExpiresAt = originalDraft.getExpiresAt();

        doPost(BEARER_TOKEN, new DraftDefendantResponseRequest(CASE_ID, PAYLOAD), DRAFT_RESPONSES_URL)
            .andExpect(status().isConflict());

        assertThat(draftStoreRepository.count()).isOne();
        DraftStoreEntity unchangedDraft = draftStoreRepository.findById(draftId)
            .orElseThrow(() -> new AssertionError("Draft defendant response should exist in DB"));
        assertThat(unchangedDraft.getCaseId()).isEqualTo(CASE_ID);
        assertThat(unchangedDraft.getPayload()).containsEntry("step", "active-test");
        assertThat(unchangedDraft.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(unchangedDraft.getExpiresAt()).isEqualTo(originalExpiresAt);
    }

    @Test
    void shouldReturnBadRequestAndNotCreateWhenCaseIdIsMissing() throws Exception {
        draftStoreRepository.deleteAll();

        doPost(BEARER_TOKEN, new DraftDefendantResponseRequest(null, PAYLOAD), DRAFT_RESPONSES_URL)
            .andExpect(status().isBadRequest());

        doPost(BEARER_TOKEN, new DraftDefendantResponseRequest("  ", PAYLOAD), DRAFT_RESPONSES_URL)
            .andExpect(status().isBadRequest());

        assertThat(draftStoreRepository.count()).isZero();
    }

    @Test
    void shouldReturnBadRequestAndNotCreateWhenPayloadIsMissing() throws Exception {
        draftStoreRepository.deleteAll();

        doPost(BEARER_TOKEN, new DraftDefendantResponseRequest(CASE_ID, null), DRAFT_RESPONSES_URL)
            .andExpect(status().isBadRequest());

        assertThat(draftStoreRepository.count()).isZero();
    }

    @Test
    void shouldReturnDraftForCase() throws Exception {
        doGet(BEARER_TOKEN, DRAFT_RESPONSE_BY_CASE_URL, CASE_ID)
            .andExpectAll(
                status().isOk(),
                jsonPath("$.draftId").value(draftId.toString()),
                jsonPath("$.caseId").value(CASE_ID),
                jsonPath("$.payload.step").value("active-test")
            );
    }

    @Test
    void shouldAllowDraftForDifferentCaseWhileAnotherExists() throws Exception {
        doPost(BEARER_TOKEN, new DraftDefendantResponseRequest(OTHER_CASE_ID, PAYLOAD), DRAFT_RESPONSES_URL)
            .andExpectAll(
                status().isCreated(),
                jsonPath("$.caseId").value(OTHER_CASE_ID)
            );

        doGet(BEARER_TOKEN, DRAFT_RESPONSE_BY_CASE_URL, CASE_ID)
            .andExpectAll(
                status().isOk(),
                jsonPath("$.draftId").value(draftId.toString())
            );

        assertThat(draftStoreRepository.findByUserIdAndDraftType(USER_ID, DRAFT_TYPE)).hasSize(2);
    }

    @Test
    void shouldAllowDraftForSameCaseWhenDifferentUser() throws Exception {
        String otherUserToken = "Bearer other-user";
        given(userService.getUserInfo(otherUserToken))
            .willReturn(UserInfo.builder().uid("user2").build());

        doPost(otherUserToken, new DraftDefendantResponseRequest(CASE_ID, PAYLOAD), DRAFT_RESPONSES_URL)
            .andExpect(status().isCreated());

        assertThat(draftStoreRepository.count()).isEqualTo(2);
    }

    @Test
    void shouldNotConflictWithDraftClaimForSameUserAndCase() throws Exception {
        OffsetDateTime now = OffsetDateTime.now();
        draftStoreRepository.save(new DraftStoreEntity(
            UUID.randomUUID(),
            USER_ID,
            OTHER_CASE_ID,
            DraftType.DRAFT_CLAIM,
            new HashMap<>(PAYLOAD),
            now,
            now,
            now.plusDays(DraftType.DRAFT_CLAIM.getRetentionDays()),
            null
        ));

        doPost(BEARER_TOKEN, new DraftDefendantResponseRequest(OTHER_CASE_ID, PAYLOAD), DRAFT_RESPONSES_URL)
            .andExpect(status().isCreated());

        assertThat(draftStoreRepository.findByUserIdAndDraftType(USER_ID, DRAFT_TYPE))
            .extracting(DraftStoreEntity::getCaseId)
            .contains(CASE_ID, OTHER_CASE_ID);
    }

    @Test
    void shouldReturnNotFoundWhenNoDraftExistsForCase() throws Exception {
        doGet(BEARER_TOKEN, DRAFT_RESPONSE_BY_CASE_URL, OTHER_CASE_ID)
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenCaseDraftIsExpired() throws Exception {
        draftStoreRepository.deleteAll();
        OffsetDateTime expiredDate = OffsetDateTime.now().minusDays(RETENTION_DAYS + 1);
        draftStoreRepository.save(draftResponse(
            draftId,
            expiredDate,
            expiredDate.plusDays(RETENTION_DAYS),
            "expired-test"
        ));

        doGet(BEARER_TOKEN, DRAFT_RESPONSE_BY_CASE_URL, CASE_ID)
            .andExpect(status().isNotFound());

        assertThat(draftStoreRepository.findById(draftId)).isPresent();
    }

    @Test
    void shouldReturnConflictWhenCreatingDraftForCaseThatHasExpiredResponse() throws Exception {
        draftStoreRepository.deleteAll();
        OffsetDateTime expiredDate = OffsetDateTime.now().minusDays(RETENTION_DAYS + 1);
        draftStoreRepository.save(draftResponse(
            draftId,
            expiredDate,
            expiredDate.plusDays(RETENTION_DAYS),
            "expired-test"
        ));

        doPost(BEARER_TOKEN, new DraftDefendantResponseRequest(CASE_ID, PAYLOAD), DRAFT_RESPONSES_URL)
            .andExpect(status().isConflict());

        assertThat(draftStoreRepository.findById(draftId)).isPresent();
        assertThat(draftStoreRepository.count()).isOne();
    }

    @Test
    void shouldReturnNotFoundWhenDifferentUserRequestsDraftForCase() throws Exception {
        String otherUserToken = "Bearer other-user";
        given(userService.getUserInfo(otherUserToken))
            .willReturn(UserInfo.builder().uid("user2").build());

        doGet(otherUserToken, DRAFT_RESPONSE_BY_CASE_URL, CASE_ID)
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateOneDraftAndRejectTheOtherWhenTwoCreatesRunConcurrently() throws Exception {
        draftStoreRepository.deleteAll();
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        List<DraftStoreEntity> created = Collections.synchronizedList(new ArrayList<>());
        List<DraftDefendantResponseAlreadyExistsException> conflicts = Collections.synchronizedList(new ArrayList<>());

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Runnable createDraft = () -> {
                try {
                    ready.countDown();
                    start.await();
                    created.add(draftDefendantResponseService.createDraftResponse(
                        USER_ID,
                        CASE_ID,
                        new HashMap<>(PAYLOAD)
                    ));
                } catch (DraftDefendantResponseAlreadyExistsException ex) {
                    conflicts.add(ex);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(ex);
                }
            };
            final Future<?> first = executor.submit(createDraft);
            final Future<?> second = executor.submit(createDraft);
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            first.get(10, TimeUnit.SECONDS);
            second.get(10, TimeUnit.SECONDS);
        }

        assertThat(created).hasSize(1);
        assertThat(conflicts).hasSize(1);
        assertThat(draftStoreRepository.findByUserIdAndDraftType(USER_ID, DRAFT_TYPE))
            .singleElement()
            .extracting(DraftStoreEntity::getId)
            .isEqualTo(created.getFirst().getId());
    }

    @Test
    void shouldRejectDuplicateDraftWhenUserAlreadyHasResponseForCase() {
        draftStoreRepository.deleteAll();
        OffsetDateTime now = OffsetDateTime.now();
        draftStoreRepository.saveAndFlush(draftResponse(
            UUID.randomUUID(),
            now,
            now.plusDays(RETENTION_DAYS),
            "original"
        ));

        assertThatThrownBy(() -> draftStoreRepository.saveAndFlush(draftResponse(
            UUID.randomUUID(),
            now,
            now.plusDays(RETENTION_DAYS),
            "duplicate"
        ))).isInstanceOf(DataIntegrityViolationException.class);

        draftStoreRepository.deleteAll();
    }

    @Test
    void shouldReturnDraftWhenDraftExists() throws Exception {
        doGet(BEARER_TOKEN, DRAFT_RESPONSE_BY_ID_URL, draftId)
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$.draftId").value(draftId.toString()),
                jsonPath("$.caseId").value(CASE_ID),
                jsonPath("$.payload.step").value("active-test")
            );
    }

    @Test
    void shouldUpdatePayloadAndTimestampWhenDraftExists() throws Exception {
        DraftStoreEntity draft = draftStoreRepository.findById(draftId)
            .orElseThrow(() -> new AssertionError("Draft defendant response should exist in DB"));
        OffsetDateTime initialUpdatedAt = draft.getUpdatedAt();
        DraftDefendantResponseRequest updatedRequest = new DraftDefendantResponseRequest(
            CASE_ID,
            Map.of("step", "updated-step")
        );

        doDraftPut(BEARER_TOKEN, updatedRequest, draftId)
            .andExpectAll(
                status().isOk(),
                jsonPath("$.draftId").value(draftId.toString()),
                jsonPath("$.caseId").value(CASE_ID),
                jsonPath("$.payload.step").value("updated-step")
            );

        DraftStoreEntity updatedEntity = draftStoreRepository.findById(draftId)
            .orElseThrow(() -> new AssertionError("Draft defendant response should exist in DB"));
        assertThat(updatedEntity.getPayload()).extracting("step").isEqualTo("updated-step");
        assertThat(updatedEntity.getUpdatedAt()).isAfter(initialUpdatedAt);
    }

    @Test
    void shouldPreserveCreationAndExpiryTimestampsWhenDraftIsUpdated() throws Exception {
        DraftStoreEntity initialDraft = draftStoreRepository.findById(draftId)
            .orElseThrow(() -> new AssertionError("Draft defendant response should exist in DB"));
        OffsetDateTime initialCreatedAt = initialDraft.getCreatedAt();
        OffsetDateTime initialExpiresAt = initialDraft.getExpiresAt();

        doDraftPut(
            BEARER_TOKEN,
            new DraftDefendantResponseRequest(CASE_ID, Map.of("step", "updated-step")),
            draftId
        ).andExpect(status().isOk());

        DraftStoreEntity updatedEntity = draftStoreRepository.findById(draftId)
            .orElseThrow(() -> new AssertionError("Draft defendant response should exist in DB"));
        assertThat(updatedEntity.getCreatedAt()).isEqualTo(initialCreatedAt);
        assertThat(updatedEntity.getExpiresAt()).isEqualTo(initialExpiresAt);
    }

    @Test
    void shouldReturnNotFoundWhenDraftIsExpired() throws Exception {
        draftStoreRepository.deleteById(draftId);
        OffsetDateTime expiredDate = OffsetDateTime.now().minusDays(RETENTION_DAYS + 1);
        draftStoreRepository.save(draftResponse(
            draftId,
            expiredDate,
            OffsetDateTime.now().minusDays(1),
            "expired-test"
        ));

        doGet(BEARER_TOKEN, DRAFT_RESPONSE_BY_ID_URL, draftId)
            .andExpect(status().isNotFound());

        doGet(BEARER_TOKEN, DRAFT_RESPONSE_BY_CASE_URL, CASE_ID)
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingExpiredDraft() throws Exception {
        draftStoreRepository.deleteById(draftId);
        OffsetDateTime expiredDate = OffsetDateTime.now().minusDays(RETENTION_DAYS + 1);
        draftStoreRepository.save(draftResponse(
            draftId,
            expiredDate,
            expiredDate.plusDays(RETENTION_DAYS),
            "expired-test"
        ));

        doDraftPut(
            BEARER_TOKEN,
            new DraftDefendantResponseRequest(CASE_ID, Map.of("step", "updated-step")),
            draftId
        ).andExpect(status().isNotFound());

        assertThat(draftStoreRepository.count()).isOne();
        DraftStoreEntity expiredDraft = draftStoreRepository.findById(draftId)
            .orElseThrow(() -> new AssertionError("Expired draft defendant response should remain in DB"));
        assertThat(expiredDraft.getPayload()).containsEntry("step", "expired-test");
    }

    @Test
    void shouldReturnNotFoundWhenDifferentUserAccessesDraft() throws Exception {
        String bearerToken2 = "Bearer other-user-token";
        given(userService.getUserInfo(bearerToken2))
            .willReturn(UserInfo.builder().uid("2").build());

        doGet(bearerToken2, DRAFT_RESPONSE_BY_ID_URL, draftId)
            .andExpect(status().isNotFound());

        doDraftPut(
            bearerToken2,
            new DraftDefendantResponseRequest(CASE_ID, Map.of("step", "unauthorised-step")),
            draftId
        ).andExpect(status().isNotFound());

        doDelete(bearerToken2, null, DRAFT_RESPONSE_BY_ID_URL, draftId)
            .andExpect(status().isNotFound());

        DraftStoreEntity draft = draftStoreRepository.findById(draftId)
            .orElseThrow(() -> new AssertionError("Draft defendant response should still exist in DB"));
        assertThat(draft.getUserId()).isEqualTo(USER_ID);
    }

    @Test
    void shouldDeleteDraftWhenOwnedByUser() throws Exception {
        assertThat(draftStoreRepository.findById(draftId)).isPresent();

        doDelete(BEARER_TOKEN, null, DRAFT_RESPONSE_BY_ID_URL, draftId)
            .andExpect(status().isNoContent());

        assertThat(draftStoreRepository.findById(draftId)).isEmpty();

        doGet(BEARER_TOKEN, DRAFT_RESPONSE_BY_ID_URL, draftId)
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldCalculateExpiryFromDraftTypeRetentionWhenDraftIsCreated() throws Exception {
        draftStoreRepository.deleteAll();
        Map<String, Object> payload = Map.of(
            "step", "response-details",
            "draftClaimCacheTtlDays", RETENTION_DAYS
        );

        MvcResult result = doPost(
            BEARER_TOKEN,
            new DraftDefendantResponseRequest(CASE_ID, payload),
            DRAFT_RESPONSES_URL
        ).andExpect(status().isCreated()).andReturn();

        UUID createdDraftId = UUID.fromString(JsonPath.read(result.getResponse().getContentAsString(), "$.draftId"));
        DraftStoreEntity draftInDb = draftStoreRepository.findById(createdDraftId)
            .orElseThrow(() -> new AssertionError("Draft defendant response should be persisted in database"));

        assertThat(draftInDb.getDraftType()).isEqualTo(DRAFT_TYPE);
        assertThat(((Number) draftInDb.getPayload().get("draftClaimCacheTtlDays")).longValue())
            .isEqualTo(RETENTION_DAYS);
        assertThat(draftInDb.getExpiresAt()).isEqualTo(
            DraftStoreService.calculateExpiresAt(draftInDb.getCreatedAt(), RETENTION_DAYS)
        );
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 14L, 9999L})
    void shouldRejectOutOfPolicyPayloadTtlWhenDraftIsCreated(long payloadTtlDays) throws Exception {
        draftStoreRepository.deleteAll();
        Map<String, Object> payload = Map.of(
            "step", "response-details",
            "draftClaimCacheTtlDays", payloadTtlDays
        );

        doPost(BEARER_TOKEN, new DraftDefendantResponseRequest(CASE_ID, payload), DRAFT_RESPONSES_URL)
            .andExpect(status().isBadRequest());

        assertThat(draftStoreRepository.count()).isZero();
    }

    private ResultActions doDraftPut(String authorisation, DraftDefendantResponseRequest request, UUID id)
        throws Exception {
        return mockMvc.perform(
            MockMvcRequestBuilders.put(DRAFT_RESPONSE_BY_ID_URL, id)
                .header(HttpHeaders.AUTHORIZATION, authorisation)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
        );
    }

    private DraftStoreEntity draftResponse(UUID id,
                                           OffsetDateTime createdAt,
                                           OffsetDateTime expiresAt,
                                           String step) {
        return new DraftStoreEntity(
            id,
            USER_ID,
            CASE_ID,
            DRAFT_TYPE,
            new HashMap<>(Map.of("step", step)),
            createdAt,
            createdAt,
            expiresAt,
            null
        );
    }
}
