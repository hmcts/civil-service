package uk.gov.hmcts.reform.civil.controllers;

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
import uk.gov.hmcts.reform.dashboard.data.DraftClaimRequest;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftClaimAlreadyExistsException;
import uk.gov.hmcts.reform.dashboard.services.DraftClaimService;
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
public class DraftClaimControllerIntegrationTest extends BaseIntegrationTest {

    private static final String DRAFT_CLAIMS_URL = "/dashboard/draft-claims";
    private static final String DRAFT_CLAIM_BY_ID_URL = "/dashboard/draft-claims/{draft-id}";
    private static final String DRAFT_CLAIM_PAYMENT_RETENTION_URL =
        "/dashboard/draft-claims/{draft-id}/payment-retention";
    private static final String ACTIVE_DRAFT_CLAIM_URL = "/dashboard/draft-claims/active";
    private static final String DRAFT_CLAIM_BY_CASE_URL = "/dashboard/draft-claims/case/{case-id}";
    private static final String USER_ID = "user1";
    private static final Map<String, Object> PAYLOAD = Map.of("step", "claimant-details");
    private static final DraftType DRAFT_TYPE = DraftType.DRAFT_CLAIM;
    private static final long RETENTION_DAYS = 30L;
    private static final long PAYMENT_RETENTION_DAYS = DRAFT_TYPE.getPaymentRetentionDays();

    @Autowired
    private DraftStoreRepository draftStoreRepository;

    @Autowired
    private DraftClaimService draftClaimService;

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
        DraftStoreEntity draftClaim = new DraftStoreEntity();
        draftClaim.setId(draftId);
        draftClaim.setUserId(USER_ID);
        draftClaim.setCaseId(null); // Set to null so this represents a blank active draft
        draftClaim.setDraftType(DRAFT_TYPE);
        draftClaim.setPayload(new HashMap<>(Map.of("step", "active-test")));
        draftClaim.setCreatedAt(now);
        draftClaim.setUpdatedAt(now);
        draftClaim.setExpiresAt(now.plusDays(RETENTION_DAYS));

        draftStoreRepository.save(draftClaim);
    }

    @AfterEach
    void tearDown() {
        draftStoreRepository.deleteAll();
    }

    @Test
    void shouldCreateAndPersistDraftWhenNoActiveDraftExists() throws Exception {
        draftStoreRepository.deleteAll();
        DraftClaimRequest request = new DraftClaimRequest(null, PAYLOAD);

        MvcResult result = doPost(BEARER_TOKEN, request, DRAFT_CLAIMS_URL)
            .andExpect(status().isCreated())
            .andExpectAll(
                jsonPath("$.draftId").exists(),
                jsonPath("$.payload.step").value("claimant-details")
            )
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        UUID createdDraftId = UUID.fromString(JsonPath.read(responseBody, "$.draftId"));

        DraftStoreEntity draftInDb = draftStoreRepository.findById(createdDraftId)
            .orElseThrow(() -> new AssertionError("Draft claim should be persisted in database"));

        assertThat(draftInDb.getPayload()).extracting("step").isEqualTo("claimant-details");
        assertThat(draftInDb.getUserId()).isEqualTo(USER_ID);
        assertThat(draftInDb.getExpiresAt()).isEqualTo(
            DraftStoreService.calculateExpiresAt(draftInDb.getCreatedAt(), RETENTION_DAYS)
        );
    }

    @Test
    void shouldReturnConflictAndLeaveDraftUnchangedWhenActiveBlankDraftExists() throws Exception {
        DraftStoreEntity originalDraft = saveInProgressDraft(draftId);
        OffsetDateTime originalCreatedAt = originalDraft.getCreatedAt();
        OffsetDateTime originalExpiresAt = originalDraft.getExpiresAt();

        doPost(BEARER_TOKEN, new DraftClaimRequest(null, PAYLOAD), DRAFT_CLAIMS_URL)
            .andExpect(status().isConflict());

        assertThat(draftStoreRepository.count()).isOne();
        DraftStoreEntity unchangedDraft = draftStoreRepository.findById(draftId)
            .orElseThrow(() -> new AssertionError("Draft claim should exist in DB"));
        assertThat(unchangedDraft.getCaseId()).isNull();
        assertThat(unchangedDraft.getPayload()).containsEntry("step", "active-test");
        assertThat(unchangedDraft.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(unchangedDraft.getExpiresAt()).isEqualTo(originalExpiresAt);
    }

    @Test
    void shouldReturnBadRequestAndNotCreateWhenCaseIdIsSentOnCreate() throws Exception {
        draftStoreRepository.deleteAll();

        doPost(BEARER_TOKEN, new DraftClaimRequest("12345", PAYLOAD), DRAFT_CLAIMS_URL)
            .andExpect(status().isBadRequest());

        assertThat(draftStoreRepository.count()).isZero();
    }

    @Test
    void shouldReturnActiveDraftForCase() throws Exception {
        draftStoreRepository.deleteAll();
        OffsetDateTime now = OffsetDateTime.now();
        draftStoreRepository.save(draftClaim(draftId, now, now.plusDays(RETENTION_DAYS), "payment-step", "12345"));

        doGet(BEARER_TOKEN, DRAFT_CLAIM_BY_CASE_URL, "12345")
            .andExpectAll(
                status().isOk(),
                jsonPath("$.draftId").value(draftId.toString()),
                jsonPath("$.caseId").value("12345"),
                jsonPath("$.payload.step").value("payment-step")
            );
    }

    @Test
    void shouldAllowNewBlankDraftWhileSubmittedDraftAwaitsPayment() throws Exception {
        // Submit: the blank draft is linked to the CCD case
        doDraftPut(BEARER_TOKEN, new DraftClaimRequest("12345", PAYLOAD), draftId)
            .andExpect(status().isOk());

        // The user starts another claim while the first is unpaid
        doPost(BEARER_TOKEN, new DraftClaimRequest(null, PAYLOAD), DRAFT_CLAIMS_URL)
            .andExpectAll(
                status().isCreated(),
                jsonPath("$.caseId").doesNotExist()
            );

        // The unpaid draft can still be loaded by its case id
        doGet(BEARER_TOKEN, DRAFT_CLAIM_BY_CASE_URL, "12345")
            .andExpectAll(
                status().isOk(),
                jsonPath("$.draftId").value(draftId.toString())
            );

        assertThat(draftStoreRepository.findByUserIdAndDraftType(USER_ID, DRAFT_TYPE)).hasSize(2);
    }

    @Test
    void shouldReturnNotFoundWhenNoDraftExistsForCase() throws Exception {
        doGet(BEARER_TOKEN, DRAFT_CLAIM_BY_CASE_URL, "12345")
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenCaseDraftIsExpired() throws Exception {
        draftStoreRepository.deleteAll();
        OffsetDateTime expiredDate = OffsetDateTime.now().minusDays(RETENTION_DAYS + 1);
        draftStoreRepository.save(draftClaim(
            draftId,
            expiredDate,
            expiredDate.plusDays(RETENTION_DAYS),
            "payment-step",
            "12345"
        ));

        doGet(BEARER_TOKEN, DRAFT_CLAIM_BY_CASE_URL, "12345")
            .andExpect(status().isNotFound());

        assertThat(draftStoreRepository.findById(draftId)).isPresent();
    }

    @Test
    void shouldReturnNotFoundWhenDifferentUserRequestsDraftForCase() throws Exception {
        draftStoreRepository.deleteAll();
        OffsetDateTime now = OffsetDateTime.now();
        draftStoreRepository.save(draftClaim(draftId, now, now.plusDays(RETENTION_DAYS), "payment-step", "12345"));
        String otherUserToken = "Bearer other-user";
        given(userService.getUserInfo(otherUserToken))
            .willReturn(UserInfo.builder().uid("user2").build());

        doGet(otherUserToken, DRAFT_CLAIM_BY_CASE_URL, "12345")
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateDraftWhenDifferentUserHasActiveDraft() throws Exception {
        String otherUserToken = "Bearer other-user";
        given(userService.getUserInfo(otherUserToken))
            .willReturn(UserInfo.builder().uid("user2").build());

        doPost(otherUserToken, new DraftClaimRequest(null, PAYLOAD), DRAFT_CLAIMS_URL)
            .andExpect(status().isCreated());

        assertThat(draftStoreRepository.count()).isEqualTo(2);
    }

    @Test
    void shouldCreateOneDraftAndRejectTheOtherWhenTwoCreatesRunConcurrently() throws Exception {
        draftStoreRepository.deleteAll();
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        List<DraftStoreEntity> created = Collections.synchronizedList(new ArrayList<>());
        List<DraftClaimAlreadyExistsException> conflicts = Collections.synchronizedList(new ArrayList<>());

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Runnable createDraft = () -> {
                try {
                    ready.countDown();
                    start.await();
                    created.add(draftClaimService.createDraftClaim(USER_ID, new HashMap<>(PAYLOAD)));
                } catch (DraftClaimAlreadyExistsException ex) {
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
    void shouldRejectDuplicateDraftWhenUserAlreadyHasDraftClaim() {
        draftStoreRepository.deleteAll(); // Clean state before testing
        OffsetDateTime now = OffsetDateTime.now();
        DraftStoreEntity duplicateDraft = draftClaim(
            UUID.randomUUID(),
            now,
            now.plusDays(RETENTION_DAYS),
            "duplicate",
            "12345"
        );

        draftStoreRepository.saveAndFlush(draftClaim(UUID.randomUUID(), now, now.plusDays(RETENTION_DAYS), "original", "12345"));
        assertThatThrownBy(() -> draftStoreRepository.saveAndFlush(duplicateDraft))
            .isInstanceOf(DataIntegrityViolationException.class);

        draftStoreRepository.deleteAll();
    }

    @Test
    void shouldReturnDraftWhenDraftExists() throws Exception {
        doGet(BEARER_TOKEN, DRAFT_CLAIM_BY_ID_URL, draftId)
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$.draftId").value(draftId.toString()),
                jsonPath("$.payload.step").value("active-test")
            );
    }

    @Test
    void shouldReturnDraftWhenActiveDraftExists() throws Exception {
        saveInProgressDraft(draftId);

        doGet(BEARER_TOKEN, ACTIVE_DRAFT_CLAIM_URL)
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$.draftId").value(draftId.toString()),
                jsonPath("$.payload.step").value("active-test")
            );
    }

    @Test
    void shouldUpdatePayloadAndTimestampWhenDraftExists() throws Exception {
        DraftStoreEntity draftClaim = draftStoreRepository.findById(draftId)
            .orElseThrow(() -> new AssertionError("Draft claim should exist in DB"));
        OffsetDateTime initialUpdatedAt = draftClaim.getUpdatedAt();

        Map<String, Object> updatedPayload = Map.of("step", "updated-step");
        DraftClaimRequest updatedRequest = new DraftClaimRequest(null, updatedPayload);

        doDraftPut(BEARER_TOKEN, updatedRequest, draftId)
            .andExpectAll(
                status().isOk(),
                jsonPath("$.draftId").value(draftId.toString()),
                jsonPath("$.payload.step").value("updated-step"));

        DraftStoreEntity updatedEntity = draftStoreRepository.findById(draftId)
            .orElseThrow(() -> new AssertionError("Draft claim should exist in DB"));

        assertThat(updatedEntity.getPayload()).extracting("step").isEqualTo("updated-step");
        assertThat(updatedEntity.getUpdatedAt()).isAfter(initialUpdatedAt);
    }

    @Test
    void shouldPreserveCreationAndExpiryTimestampsWhenDraftIsUpdated() throws Exception {
        DraftStoreEntity initialDraft = draftStoreRepository.findById(draftId)
            .orElseThrow(() -> new AssertionError("Draft claim should exist in db"));
        OffsetDateTime initialCreatedAt = initialDraft.getCreatedAt();
        OffsetDateTime initialExpiresAt = initialDraft.getExpiresAt();

        Map<String, Object> updatedPayload = Map.of("step", "updated-step");
        DraftClaimRequest updatedRequest = new DraftClaimRequest(null, updatedPayload);

        doDraftPut(BEARER_TOKEN, updatedRequest, draftId)
            .andExpect(status().isOk());

        DraftStoreEntity updatedEntity = draftStoreRepository.findById(draftId)
            .orElseThrow(() -> new AssertionError("Draft claim should exist in DB"));

        assertThat(updatedEntity.getCreatedAt()).isEqualTo(initialCreatedAt);
        assertThat(updatedEntity.getExpiresAt()).isEqualTo(initialExpiresAt);
    }

    @Test
    void shouldSetExpiryTo30DaysAfterDraftCreationWhenDraftIsCreated() {
        DraftStoreEntity draftInDB = draftStoreRepository.findById(draftId)
            .orElseThrow(() -> new AssertionError("Draft claim should exist in DB"));

        assertThat(draftInDB.getExpiresAt()).isEqualTo(draftInDB.getCreatedAt().plusDays(30));
    }

    @Test
    void shouldReturnNotFoundWhenDraftIsExpired() throws Exception {
        draftStoreRepository.deleteById(draftId);
        OffsetDateTime expiredDate = OffsetDateTime.now().minusDays(RETENTION_DAYS + 1);

        draftStoreRepository.save(draftClaim(
            draftId,
            expiredDate,
            OffsetDateTime.now().minusDays(1)
        ));

        doGet(BEARER_TOKEN, DRAFT_CLAIM_BY_ID_URL, draftId)
            .andExpect(status().isNotFound());

        doGet(BEARER_TOKEN, ACTIVE_DRAFT_CLAIM_URL)
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingExpiredDraft() throws Exception {
        draftStoreRepository.deleteById(draftId);
        OffsetDateTime expiredDate = OffsetDateTime.now().minusDays(RETENTION_DAYS + 1);
        draftStoreRepository.save(draftClaim(
            draftId,
            expiredDate,
            expiredDate.plusDays(RETENTION_DAYS)
        ));

        DraftClaimRequest updateRequest = new DraftClaimRequest(
            "updated-case-id",
            Map.of("step", "updated-step")
        );

        doDraftPut(BEARER_TOKEN, updateRequest, draftId)
            .andExpect(status().isNotFound());

        assertThat(draftStoreRepository.count()).isOne();
        DraftStoreEntity expiredDraft = draftStoreRepository.findById(draftId)
            .orElseThrow(() -> new AssertionError("Expired draft claim should remain in DB"));
        assertThat(expiredDraft.getPayload()).containsEntry("step", "expired-test");
    }

    @Test
    void shouldReplaceExpiredDraftWhenCreatingDraftClaim() throws Exception {
        draftStoreRepository.deleteById(draftId);
        OffsetDateTime expiredDate = OffsetDateTime.now().minusDays(RETENTION_DAYS + 1);
        draftStoreRepository.save(draftClaim(
            draftId,
            expiredDate,
            expiredDate.plusDays(RETENTION_DAYS),
            "expired-test",
            null
        ));

        MvcResult result = doPost(
            BEARER_TOKEN,
            new DraftClaimRequest(null, PAYLOAD),
            DRAFT_CLAIMS_URL
        )
            .andExpect(status().isCreated())
            .andReturn();

        UUID newDraftId = UUID.fromString(JsonPath.read(result.getResponse().getContentAsString(), "$.draftId"));
        assertThat(newDraftId).isNotEqualTo(draftId);
        assertThat(draftStoreRepository.findById(draftId)).isEmpty();
        assertThat(draftStoreRepository.count()).isOne();

        DraftStoreEntity newDraft = draftStoreRepository.findById(newDraftId)
            .orElseThrow(() -> new AssertionError("New draft claim should exist in DB"));
        assertThat(newDraft.getExpiresAt()).isEqualTo(
            DraftStoreService.calculateExpiresAt(newDraft.getCreatedAt(), RETENTION_DAYS)
        );
    }

    @Test
    void shouldReturnNotFoundWhenDifferentUserAccessesDraft() throws Exception {
        String bearerToken2 = "Bearer jgiofdjbinaiogokfabinnaojpefjeapb.user2";

        given(userService.getUserInfo(bearerToken2))
            .willReturn(UserInfo.builder().uid("2").build());

        doGet(bearerToken2, DRAFT_CLAIM_BY_ID_URL, draftId)
            .andExpect(status().isNotFound());

        DraftClaimRequest updateRequest = new DraftClaimRequest("1234", Map.of("step", "unauthorised-step"));
        doDraftPut(bearerToken2, updateRequest, draftId)
            .andExpect(status().isNotFound());

        doDelete(bearerToken2, null, DRAFT_CLAIM_BY_ID_URL, draftId)
            .andExpect(status().isNotFound());

        DraftStoreEntity draftClaim = draftStoreRepository.findById(draftId)
            .orElseThrow(() -> new AssertionError("Draft claim should still exist in DB"));

        assertThat(draftClaim.getUserId()).isEqualTo(USER_ID);
    }

    @Test
    void shouldDeleteDraftWhenOwnedByUser() throws Exception {
        assertThat(draftStoreRepository.findById(draftId)).isPresent();

        doDelete(BEARER_TOKEN, null, DRAFT_CLAIM_BY_ID_URL, draftId)
            .andExpect(status().isNoContent());

        assertThat(draftStoreRepository.findById(draftId)).isEmpty();

        doGet(BEARER_TOKEN, DRAFT_CLAIM_BY_ID_URL, draftId)
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldCalculateExpiryFromDraftTypeRetentionWhenDraftIsCreated() throws Exception {
        draftStoreRepository.deleteAll();
        Map<String, Object> payload = Map.of(
            "step", "claimant-details",
            "draftClaimCacheTtlDays", RETENTION_DAYS
        );

        MvcResult result = doPost(BEARER_TOKEN, new DraftClaimRequest(null, payload), DRAFT_CLAIMS_URL)
            .andExpect(status().isCreated())
            .andReturn();

        UUID createdDraftId = UUID.fromString(JsonPath.read(result.getResponse().getContentAsString(), "$.draftId"));
        DraftStoreEntity draftInDb = draftStoreRepository.findById(createdDraftId)
            .orElseThrow(() -> new AssertionError("Draft claim should be persisted in database"));

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
            "step", "claimant-details",
            "draftClaimCacheTtlDays", payloadTtlDays
        );

        doPost(BEARER_TOKEN, new DraftClaimRequest(null, payload), DRAFT_CLAIMS_URL)
            .andExpect(status().isBadRequest());

        assertThat(draftStoreRepository.count()).isZero();
    }

    @Test
    void shouldShortenExpiryToPaymentRetentionWhenRequested() throws Exception {
        DraftStoreEntity before = draftStoreRepository.findById(draftId)
            .orElseThrow(() -> new AssertionError("Draft claim should exist in DB"));
        OffsetDateTime originalExpiry = before.getExpiresAt();

        MvcResult result = doPut(BEARER_TOKEN, null, DRAFT_CLAIM_PAYMENT_RETENTION_URL, draftId)
            .andExpect(status().isOk())
            .andReturn();

        DraftStoreEntity after = draftStoreRepository.findById(draftId)
            .orElseThrow(() -> new AssertionError("Draft claim should still exist in DB"));
        assertThat(after.getExpiresAt()).isBefore(originalExpiry);
        assertThat(after.getExpiresAt()).isEqualTo(
            DraftStoreService.calculateExpiresAt(after.getUpdatedAt(), PAYMENT_RETENTION_DAYS)
        );
        assertThat(JsonPath.<String>read(result.getResponse().getContentAsString(), "$.expiresAt"))
            .isNotBlank();
    }

    private ResultActions doDraftPut(String authorisation, DraftClaimRequest request, UUID draftClaimId)
        throws Exception {
        return mockMvc.perform(
            MockMvcRequestBuilders.put(DRAFT_CLAIM_BY_ID_URL, draftClaimId)
                .header(HttpHeaders.AUTHORIZATION, authorisation)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
        );
    }

    private DraftStoreEntity saveInProgressDraft(UUID id) {
        draftStoreRepository.deleteAll();
        OffsetDateTime now = OffsetDateTime.now();
        draftStoreRepository.saveAndFlush(draftClaim(
            id,
            now,
            now.plusDays(RETENTION_DAYS),
            "active-test",
            null
        ));
        return draftStoreRepository.findById(id)
            .orElseThrow(() -> new AssertionError("In-progress draft should exist in DB"));
    }

    private DraftStoreEntity draftClaim(UUID id,
                                        OffsetDateTime createdAt,
                                        OffsetDateTime expiresAt) {
        return draftClaim(id, createdAt, expiresAt, "expired-test", null);
    }

    private DraftStoreEntity draftClaim(UUID id,
                                        OffsetDateTime createdAt,
                                        OffsetDateTime expiresAt,
                                        String step,
                                        String caseId) {
        return new DraftStoreEntity(
            id,
            USER_ID,
            caseId,
            DRAFT_TYPE,
            new HashMap<>(Map.of("step", step)),
            createdAt,
            createdAt,
            expiresAt
        );
    }
}
